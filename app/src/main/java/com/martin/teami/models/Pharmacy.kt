package com.martin.teami.models

import com.google.android.gms.maps.model.LatLng
import com.google.gson.annotations.SerializedName

class Pharmacy(
    var name: String,
    var street: String,
    @SerializedName("organisation_id")
    var organizationId: String,
    @SerializedName("reign_id")
    var regionId:String,
    var latitude:String,
    var longitude:String,
    @SerializedName("work_time")
    var workTime:String,
    var token:String,
    @SerializedName("phone_id")
    var phoneID:String
)