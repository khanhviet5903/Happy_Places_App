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
import com.example.happyplacesapp.databinding.ItemHappyPlaceBinding
import com.example.happyplacesapp.model.HappyPlaceModel
import androidx.core.net.toUri

open class HappyPlacesAdapter (
    private var context: Context,
    private val list: ArrayList<HappyPlaceModel>
): RecyclerView.Adapter<RecyclerView.ViewHolder> () {
    private var onClickListener : OnClickListener? = null

    interface OnClickListener {
        fun onClick(position: Int, model: HappyPlaceModel)
}
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            ItemHappyPlaceBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(binding: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]

        if (binding is MyViewHolder) {
            binding.itemView.item_imageView.setImageURI(model.image.toUri())
            binding.itemView.item_txt_title.text = model.title
            binding.itemView.item_txt_description.text = model.description
            Log.d("TAG",  binding.itemView.item_txt_title.text.toString())
            binding.itemView.setOnClickListener {
                if(onClickListener != null){
                    onClickListener!!.onClick(position, model)
                }
            }
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

    fun removeAt(position: Int){
        val dbHandler = DatabaseHandler(context)
        val isDelete = dbHandler.deleteHappyPlace(list[position])
        if(isDelete>0){
            list.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    private class MyViewHolder(binding: ItemHappyPlaceBinding) :
        RecyclerView.ViewHolder(binding.root)
}