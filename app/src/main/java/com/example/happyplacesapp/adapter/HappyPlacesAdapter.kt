package com.example.happyplacesapp.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.happyplacesapp.AddHappyPlaceActivity
import com.example.happyplacesapp.MainActivity
import com.example.happyplacesapp.database.DataBaseHandler
import com.example.happyplacesapp.databinding.ItemHappyPlaceBinding
import com.example.happyplacesapp.model.HappyPlaceModel
import androidx.core.net.toUri
import java.io.File

open class HappyPlacesAdapter(
    private var context: Context,
    private val list: ArrayList<HappyPlaceModel>
): RecyclerView.Adapter<HappyPlacesAdapter.MyViewHolder>() {

    private var onClickListener: OnClickListener? = null

    interface OnClickListener {
        fun onClick(position: Int, model: HappyPlaceModel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            ItemHappyPlaceBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val model = list[position]

        // Set image with null safety
        try {
            if (model.image.isNullOrEmpty() ) {
                holder.binding.itemImageView.setImageURI(Uri.parse(model.image))
            }
        } catch (e: Exception) {
            Log.e("HappyPlacesAdapter", "Error loading image: ${e.message}")
            // You might want to set a default image here
        }

        holder.binding.itemTxtTitle.text = model.title
        holder.binding.itemTxtDescription.text = model.description

        Log.d("TAG", "Title: ${model.title}")

        holder.itemView.setOnClickListener {
            onClickListener?.onClick(position, model)
        }
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun notifyEditItem(activity: Activity, position: Int, requestCode: Int) {
        val intent = Intent(context, AddHappyPlaceActivity::class.java)
        intent.putExtra(MainActivity.EXTRA_PLACE_DETAILS, list[position])

        (activity as MainActivity).resultLauncher.launch(intent)
        notifyItemChanged(position)
    }

    fun removeAt(position: Int) {
        val dbHandler = DataBaseHandler(context)
        val isDeleted = dbHandler.deleteHappyPlace(list[position])
        if (isDeleted > 0) {
            list.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    class MyViewHolder(val binding: ItemHappyPlaceBinding) : RecyclerView.ViewHolder(binding.root)
}