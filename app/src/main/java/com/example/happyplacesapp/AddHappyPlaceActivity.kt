package com.example.happyplacesapp

import androidx.appcompat.app.AppCompatActivity
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.example.happyplacesapp.R
import com.example.happyplacesapp.database.DataBaseHandler
import com.example.happyplacesapp.databinding.ActivityAddHappyPlaceBinding
import com.example.happyplacesapp.model.HappyPlaceModel
import com.example.happyplacesapp.utils.GetAddressFromLatLng
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*
import androidx.core.net.toUri

class AddHappyPlaceActivity: AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityAddHappyPlaceBinding
    private var cal = Calendar.getInstance()
    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener

    private var savedImageToInternalStorage: Uri?= null
    private var latitude: Double=0.0
    private var longitude: Double=0.0
    private var happyPlacesDetails: HappyPlaceModel? = null
    private lateinit var fusedlocationProviderClient: FusedLocationProviderClient

    companion object {
        private const val IMAGE_DIRECTORY = "HappyPlacesImages"
    }
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val lastLocation: Location? = locationResult.lastLocation
            lastLocation?.let {location ->
            latitude = lastLocation.latitude
            longitude = lastLocation.longitude
            val addressTask = GetAddressFromLatLng(this@AddHappyPlaceActivity, latitude, longitude)

            addressTask.setAddressListener(object: GetAddressFromLatLng.AddressListener{
                override fun onAddressFound(address: String?) {
                    runOnUiThread {
                        binding.etLocation.setText(address)
                    }
        }
                override fun onError() {
                    Log.d("TAG", "onError:couldn't get location")
                }
        })
        addressTask.getAddress()
    }
}
private var cameraResultLauncher: ActivityResultLauncher<Intent> =
    registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val bitmap = result.data?.extras?.get("data") as Bitmap
            // set photo
            binding.ivPlaceImage.setImageBitmap(bitmap)
            savedImageToInternalStorage = saveImageToInternalStorage(bitmap)
            Log.d("LOG", savedImageToInternalStorage.toString())
        }
    }
private var galleryResultLauncher: ActivityResultLauncher<Intent> =
    registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val contentUri = result.data?.data
            try {
                val selectedImageBitmap =
                    MediaStore.Images.Media.getBitmap(this.contentResolver, contentUri)
                binding.ivPlaceImage.setImageBitmap(selectedImageBitmap)
                savedImageToInternalStorage = saveImageToInternalStorage(selectedImageBitmap)
                Log.d("LOG", savedImageToInternalStorage.toString())
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
 private var locationResultLauncher: ActivityResultLauncher<Intent> =
     registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
         val status: Status = Autocomplete.getStatusFromIntent(result.data!!)
         Log.d("TAG", status.toString())
         if (result.resultCode == Activity.RESULT_OK) {
             val place = Autocomplete.getStatusFromIntent(result.data!!)
             binding.etLocation.setText(place.address)
             latitude = place.latLng!!.latitude
             longitude = place.latLng!!.longitude
         }
     }
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    initView()
    fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
    if (!Places.isInitialized()) {
        Places.initialize(this@AddHappyPlaceActivity, resources.getString(R.string.MAPS_KEY))
    }
    binding.etDate.setOnClickListener(this)
    binding.tvAddImage.setOnClickListener(this)
    binding.btnSave.setOnClickListener(this)
    binding.etLocation.setOnClickListener(this)
    binding.tvSelectCurrentLocation.setOnClickListener(this)
}
@SuppressLint("MissingPermission")
private fun requestNewLocationData() {
    var locationRequest = LocationRequest.create().apply {
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        interval = 1000 // how many ms we want to run the request
        numUpdates = 1 //how many updated
    }

    Looper.myLooper()?.let {
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallBack,
            it
        )
    }
}

private fun initView() {
    binding = ActivityAddHappyPlaceBinding.inflate(layoutInflater)
    setContentView(binding.root)

    setSupportActionBar(binding.toolBarAddPlaces)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    binding.toolBarAddPlaces.setNavigationOnClickListener {
        onBackPressed()
    }

    if (intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)) {
        happyPlacesDetails =
            intent.getParcelableExtra(MainActivity.EXTRA_PLACE_DETAILS) as HappyPlaceModel?
    }

    dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
        cal.set[Calendar.YEAR] = year
        cal.set(Calendar.MONTH, month)
        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        updateDateView()
    }
    updateDateView()
    initHappyPlace()
}

private fun updateDateView() {
    val myFormat = "dd.MM.yyyy"
    val simpleDateFormat = SimpleDateFormat(myFormat, Locale.getDefault())
    binding.etDate.setText(simpleDateFormat.format(cal.time).toString())
}

private fun initHappyPlace() {
    if (happyPlacesDetails != null) {
        supportActionBar?.title = "Edit Happy Place"
        binding.etTitle.setText(happyPlacesDetails!!.title)
        binding.etDescription.setText(happyPlacesDetails!!.description)
        binding.etDate.setText(happyPlacesDetails!!.date)
        binding.etLocation.setText(happyPlacesDetails!!.location)
        latitude = happyPlacesDetails!!.latitude
        longitude = happyPlacesDetails!!.longitude

        savedImageToInternalStorage = happyPlacesDetails!!.image.toUri()
        binding.ivPlaceImage.setImageURI(savedImageToInternalStorage)

        binding.btnSave.text = "UPDATE"
    }
}

override fun onClick(view: View?) {
    when (view!!.id) {
        binding.etDate.id -> {
            datePickerDialog()
        }
        binding.tvAddImage.id -> {
            imagePickerDialog()
        }
        binding.btnSave.id -> {
            saveToDB()
        }
        binding.etLocation.id -> {
            locationPick()
        }
        binding.tvSelectCurrentLocation.id -> {
            getCurrentLocation()
        }
    }
}

private fun isLocationEnabled(): Boolean {
    val locationManager: LocationManager =
        getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
        LocationManager.NETWORK_PROVIDER
    )
}

private fun getCurrentLocation() {
    if (!isLocationEnabled()) {
        Toast.makeText(
            this@AddHappyPlaceActivity,
            "Your location provider is turned off! Please turn it on",
            Toast.LENGTH_SHORT
        ).show()
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        startActivity(intent)
    } else {
        Dexter.withContext(this).withPermissions(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ).withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                if (report!!.areAllPermissionsGranted()) {
                    requestNewLocationData()
                }
            }

            override fun onPermissionRationaleShouldBeShown(
                permissons: MutableList<PermissionRequest>,
                token: PermissionToken?
            ) {
                showRationalDialogForPermissions()
            }
        }).onSameThread().check()
    }
}

private fun locationPick() {
    try {
        // These are the list of fields which we required is passed
        val fields = listOf(
            Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG,
            Place.Field.ADDRESS
        )
        // Start the autocomplete intent with a unique request code.
        val intent =
            Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                .build(this@AddHappyPlaceActivity)
        locationResultLauncher.launch(intent)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun datePickerDialog() {
    DatePickerDialog(
        this@AddHappyPlaceActivity,
        dateSetListener,
        cal.get(Calendar.YEAR),
        cal.get(Calendar.MONTH),
        cal.get(Calendar.DAY_OF_WEEK)
    ).show()
}

private fun imagePickerDialog() {
    val pictureDialog = AlertDialog.Builder(this)
    pictureDialog.setTitle("Select Action:")
    val pictureDialogItems =
        arrayOf("Select photo from Gallery", "Capture photo from camera")
    pictureDialog.setItems(pictureDialogItems) { _, which ->
        when (which) {
            0 -> choosePhotoFromGallery()
            1 -> takePicture()
        }
    }
    pictureDialog.show()
}

private fun takePicture() {
    Dexter.withContext(this).withPermission(
        android.Manifest.permission.CAMERA
    ).withListener(object : PermissionListener {
        override fun onPermissionGranted(report: PermissionGrantedResponse?) {
            Toast.makeText(
                this@AddHappyPlaceActivity,
                "Camera permission is granted",
                Toast.LENGTH_LONG
            ).show()
            //Intent starts the camera
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraResultLauncher.launch(intent)
        }

        override fun onPermissionDenied(report: PermissionDeniedResponse?) {
            Toast.makeText(
                this@AddHappyPlaceActivity,
                "Camera permission is denied",
                Toast.LENGTH_LONG
            ).show()
        }

        override fun onPermissionRationaleShouldBeShown(
            permission: PermissionRequest,
            token: PermissionToken?
        ) {
            showRationalDialogForPermissions()
        }
    }).onSameThread().check()
}

private fun choosePhotoFromGallery() {
    Dexter.withContext(this).withPermissions(
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
    ).withListener(object : MultiplePermissionsListener {
        override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
            if (report!!.areAllPermissionsGranted()) {
                val galleryIntent =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                galleryResultLauncher.launch(galleryIntent)
            }
        }

        override fun onPermissionRationaleShouldBeShown(
            permissons: MutableList<PermissionRequest>,
            token: PermissionToken?
        ) {
            showRationalDialogForPermissions()
        }
    }).onSameThread().check()
}

private fun saveToDB() {
    when {
        binding.etTitle.text.isNullOrEmpty() -> {
            Toast.makeText(this, "Please enter title", Toast.LENGTH_LONG).show()
        }
        binding.etDescription.text.isNullOrEmpty() -> {
            Toast.makeText(this, "Please enter description", Toast.LENGTH_LONG).show()
        }
        binding.etLocation.text.isNullOrEmpty() -> {
            Toast.makeText(this, "Please enter location", Toast.LENGTH_LONG).show()
        }
        savedImageToInternalStorage == null -> {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_LONG).show()
        }
        else -> {
            val happyPlaceModel = HappyPlaceModel(
                if (happyPlacesDetails == null) 0 else happyPlacesDetails!!.id,
                binding.etTitle.text.toString(),
                savedImageToInternalStorage.toString(),
                binding.etDescription.text.toString(),
                binding.etDate.text.toString(),
                binding.etLocation.text.toString(),
                latitude,
                longitude
            )
            val dbHandler = DatabaseHandler(this)
            if (happyPlacesDetails == null) {
                val addHappyPlace = dbHandler.addHappyPlace(happyPlaceModel)
                if (addHappyPlace > 0) {
                    Toast.makeText(
                        this,
                        "The happy place details are inserted successfully!",
                        Toast.LENGTH_LONG
                    ).show()
                    setResult(Activity.RESULT_OK)
                    finish()
                }
            } else {
                val updateHappyPlace = dbHandler.updateHappyPlace(happyPlaceModel)
                if (updateHappyPlace > 0) {
                    Toast.makeText(
                        this,
                        "The happy place details were updated successfully!",
                        Toast.LENGTH_LONG
                    ).show()
                    setResult(Activity.RESULT_OK)
                    finish()
                }
            }
        }
    }
}

private fun saveImageToInternalStorage(bitmap: Bitmap): Uri {
    val wrapper = ContextWrapper(applicationContext)
    var file = wrapper.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE)
    file = File(file, "${UUID.randomUUID()}.jpg")

    try {
        val stream: OutputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        stream.flush()
        stream.close()
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return file.absolutePath.toUri()
}

private fun showRationalDialogForPermissions() {
    AlertDialog.Builder(this)
        .setMessage(
            "It looks like you have turned off permission that is required for this" +
                    " feature. It can be enabled under the Application Settings"
        )
        .setPositiveButton("Go to settings")
        { _, _ ->
            try {
                //Settings page
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                //Send the user to the application settings where he can directly change the permissions
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                e.printStackTrace()
            }
        }
        .setNegativeButton("Cancel")
        { dialog, _ ->
            dialog.dismiss()
        }
        .show()
}
}
