package com.example.happyplacesapp.utils

import android.content.Context
import android.location.Address
import android.location.Geocoder
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class GetAddressFromLatLng (
    context: Context,
    private val latitude: Double,
    private val longitude: Double
){
    private var executor: ExecutorService = Executors.newSingleThreadExecutor()
    private var geocoder: Geocoder = Geocoder(context, Locale.getDefault())
    private lateinit var addressListener: AddressListener

    interface AddressListener {
        fun onAddressFound(address: String?)
        fun onError()
    }

    fun setAddressListener(addressListener: AddressListener) {
        this.addressListener = addressListener
}
    fun getAddress() {
        executor.execute {
            val addressList: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)
            if (addressList != null && addressList.isNotEmpty()) {
                val address: Address = addressList[0]
                val stringBuilder = StringBuilder()
                for (i in 0..address.maxAddressLineIndex) {
                    stringBuilder.append(address.getAddressLine(i)).append(" ")
                }
                stringBuilder.deleteCharAt(stringBuilder.length - 1)
                addressListener.onAddressFound(stringBuilder.toString())
            } else {
                addressListener.onError()
            }
        }
    }

}