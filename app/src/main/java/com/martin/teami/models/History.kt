package com.martin.teami.models

import com.google.gson.annotations.SerializedName

class History(
    @SerializedName("DoctorName")
    var doctorName: String,
    var speciality: String,
    @SerializedName("class")
    var classX: String,
    @SerializedName("count_of_visit")
    var countOfVisit: Int,
    @SerializedName("Target_of_visit")
    var targetOfVisit: Int
)