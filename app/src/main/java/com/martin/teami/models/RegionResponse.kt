package com.martin.teami.models

import com.google.gson.annotations.SerializedName

class RegionResponse(
    @SerializedName("Reigns")
    var reigns:List<Resource>
)