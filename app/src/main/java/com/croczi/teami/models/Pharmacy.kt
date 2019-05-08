package com.croczi.teami.models

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
    var token:String,
    @SerializedName("phone_id")
    var phoneID:String
)