package com.croczi.teami.models

import com.google.gson.annotations.SerializedName

class RegionResponse(
    @SerializedName("Reigns")
    var regions:List<Resource>
)