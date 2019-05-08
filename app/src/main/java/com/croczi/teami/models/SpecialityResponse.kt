package com.croczi.teami.models

import com.google.gson.annotations.SerializedName

class SpecialityResponse(
    @SerializedName("Specialities")
    var specialities: List<Resource>
)