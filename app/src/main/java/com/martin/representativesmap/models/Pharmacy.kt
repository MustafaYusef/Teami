package com.martin.representativesmap.models

import com.google.android.gms.maps.model.LatLng

class Pharmacy(
    var name: String,
    var _id: String,
    var latitude: Double,
    var longitude: Double,
    var registered: Boolean
) {

    fun getPos(): LatLng {
        return LatLng(latitude, longitude)
    }
}