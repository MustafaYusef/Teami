package com.martin.teami.models

import com.google.gson.annotations.SerializedName

class Doctor(
    var name: String,
    var street: String,
    @SerializedName("organisation_id")
    var organizationId: String,
    @SerializedName("speciality_id")
    var specialityId: String,
    @SerializedName("reign_id")
    var regionId:String,
    @SerializedName("hospital_id")
    var hospitalId:String,
    var latitude:String,
    var longitude:String,
    @SerializedName("work_time")
    var workTime:String,
    var token:String,
    @SerializedName("phone_id")
    var phoneID:String
)