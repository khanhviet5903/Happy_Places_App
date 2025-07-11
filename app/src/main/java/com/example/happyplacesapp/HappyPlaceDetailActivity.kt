package com.example.happyplacesapp

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.happyplacesapp.model.HappyPlaceModel

class HappyPlaceDetailActivity : AppCompatActivity() {
    private var happyPlaceDetailModel: HappyPlaceModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_happy_place_see_details)

        // Get the HappyPlaceModel from intent
        happyPlaceDetailModel = getHappyPlaceFromIntent()

        if (happyPlaceDetailModel != null) {
            setupToolbar()
            populateViews()
            setupMapButton()
        } else {
            // Handle case where no data was passed
            finish()
        }
    }

    private fun getHappyPlaceFromIntent(): HappyPlaceModel? {
        return if (intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(MainActivity.EXTRA_PLACE_DETAILS, HappyPlaceModel::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(MainActivity.EXTRA_PLACE_DETAILS) as HappyPlaceModel?
            }
        } else {
            null
        }
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolBar_happy_place_detail)
        happyPlaceDetailModel?.let { model ->
            setSupportActionBar(toolbar)
            supportActionBar?.apply {
                setDisplayHomeAsUpEnabled(true)
                title = model.title
            }
            toolbar.setNavigationOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    private fun populateViews() {
        val ivPlaceImage = findViewById<ImageView>(R.id.iv_place_image)
        val tvDescription = findViewById<TextView>(R.id.tv_description)
        val tvLocation = findViewById<TextView>(R.id.tv_location)

        happyPlaceDetailModel?.let { model ->
            try {
                // Set image from URI
                val imageUri = Uri.parse(model.image)
                ivPlaceImage.setImageURI(imageUri)
            } catch (e: Exception) {
                // Handle image loading error
                e.printStackTrace()
                // You might want to set a default image here
                // ivPlaceImage.setImageResource(R.drawable.ic_default_image)
            }

            tvDescription.text = model.description
            tvLocation.text = model.location
        }
    }

    private fun setupMapButton() {
        val btnViewMap = findViewById<Button>(R.id.btn_viewMap)
        btnViewMap.setOnClickListener {
            happyPlaceDetailModel?.let { model ->
                // Comment out or remove this if MapActivity doesn't exist yet
                // val intent = Intent(this, MapActivity::class.java)
                // intent.putExtra(MainActivity.EXTRA_PLACE_DETAILS, model)
                // startActivity(intent)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}