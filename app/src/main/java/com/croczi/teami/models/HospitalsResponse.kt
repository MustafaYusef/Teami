package com.croczi.teami.models

import com.google.gson.annotations.SerializedName

class HospitalsResponse(
    @SerializedName("Hospitals")
    var hospitals:List<Resource>)
