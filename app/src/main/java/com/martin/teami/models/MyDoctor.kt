package com.martin.teami.models
import com.google.gson.annotations.SerializedName

class MyDoctor(
    @SerializedName("id")
    var id: Int?,
    @SerializedName("name")
    var name: String?,
    @SerializedName("work_time")
    var workTime: String?,
    @SerializedName("street")
    var street: String?,
    @SerializedName("latitude")
    var latitude: String?,
    @SerializedName("longitude")
    var longitude: String?,
    @SerializedName("speciality_id")
    var specialityId: Int?,
    @SerializedName("reign_id")
    var reignId: Int?,
    @SerializedName("organisation_id")
    var organisationId: Int?,
    @SerializedName("hospital_id")
    var hospitalId: Int?,
    @SerializedName("speciality")
    var speciality: Resource?,
    @SerializedName("reign")
    var reign: Resource?,
    @SerializedName("organisation")
    var organisation: Resource?,
    @SerializedName("hospital")
    var hospital: Resource?
)