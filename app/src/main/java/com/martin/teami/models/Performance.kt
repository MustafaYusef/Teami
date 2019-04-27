package com.martin.teami.models

import com.google.gson.annotations.SerializedName

class Performance(
    @SerializedName("count_of_doctors")
    var countOfDoctors: Int,
    @SerializedName("class")
    var classX: String,
    @SerializedName("Target_of_visit")
    var targetOfVisit: Int,
    @SerializedName("count_of_visit")
    var countOfVisit: Int,
    @SerializedName("accepted_level")
    var acceptedLevel: String,
    @SerializedName("Activation_Ratio")
    var activationRatio: String
)