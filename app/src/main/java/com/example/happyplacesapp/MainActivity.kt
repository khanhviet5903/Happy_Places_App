package com.example.happyplacesapp

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.happyplacesapp.adapter.HappyPlacesAdapter
import com.example.happyplacesapp.database.DataBaseHandler
import com.example.happyplacesapp.databinding.ActivityMainBinding
import com.example.happyplacesapp.model.HappyPlaceModel
import com.example.happyplacesapp.utils.SwipeToDeleteCallback

import com.example.happyplacesapp.utils.SwipeToEditCallback

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    companion object {
        private const val ADD_PLACE_ACTIVITY_REQUEST_CODE = 1
        var EXTRA_PLACE_DETAILS = "extra_place_details"
    }

    var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            Log.d("TAG", result.resultCode.toString())
            if (result.resultCode == Activity.RESULT_OK) {
                //val data: Intent? = result.data
                Log.d("TAG", "happy place added")
                getHappyPlacesListFromLocalDB()
            } else {
                Log.d("TAG", "Cancelled or back pressed")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.floatingActionBarAdd.setOnClickListener {
            val intent = Intent(this, AddHappyPlaceActivity::class.java)
            resultLauncher.launch(intent)
        }
        getHappyPlacesListFromLocalDB()
    }

    private fun setUpHappyPlacesRecyclerView(happyPlacesList: ArrayList<HappyPlaceModel>) {
        binding.happyPlacesRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.happyPlacesRecyclerView.setHasFixedSize(true)
        val placesAdapter = HappyPlacesAdapter(this, happyPlacesList)
        binding.happyPlacesRecyclerView.adapter = placesAdapter

        placesAdapter.setOnClickListener(object : HappyPlacesAdapter.OnClickListener {
            override fun onClick(position: Int, model: HappyPlaceModel) {
                val intent = Intent(this@MainActivity, HappyPlaceDetailActivity::class.java)
                intent.putExtra(EXTRA_PLACE_DETAILS, model)
                startActivity(intent)
            }
        })

        val editSwipeHandler = object : SwipeToEditCallback(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = binding.happyPlacesRecyclerView.adapter as HappyPlacesAdapter
                adapter.notifyEditItem(
                    this@MainActivity,
                    viewHolder.absoluteAdapterPosition,
                    ADD_PLACE_ACTIVITY_REQUEST_CODE
                )
            }
        }
        val editItemTouchHelper = ItemTouchHelper(editSwipeHandler)
        editItemTouchHelper.attachToRecyclerView(binding.happyPlacesRecyclerView)

        val deleteSwipeHandler = object : SwipeToDeleteCallback(this){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = binding.happyPlacesRecyclerView.adapter as HappyPlacesAdapter
                adapter.removeAt(viewHolder.absoluteAdapterPosition)
                //Check the database again
                getHappyPlacesListFromLocalDB()
            }
        }
        val deleteItemTouchHelper = ItemTouchHelper(deleteSwipeHandler)
        deleteItemTouchHelper.attachToRecyclerView(binding.happyPlacesRecyclerView)

    }

    private fun getHappyPlacesListFromLocalDB() {
        val dbHandler = DataBaseHandler(this)
        val happyPlaceList: ArrayList<HappyPlaceModel> = dbHandler.getHappyPlacesList()

        if (happyPlaceList.size > 0) {
            binding.happyPlacesRecyclerView.visibility = View.VISIBLE
            binding.txtNoRecordsAvailable.visibility = View.GONE
            setUpHappyPlacesRecyclerView(happyPlaceList)
        } else {
            binding.happyPlacesRecyclerView.visibility = View.GONE
            binding.txtNoRecordsAvailable.visibility = View.VISIBLE
        }

    }
}