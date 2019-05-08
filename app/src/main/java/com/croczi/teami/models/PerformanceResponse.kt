package com.croczi.teami.models
import com.google.gson.annotations.SerializedName


class PerformanceResponse(
    @SerializedName("Performance")
    var performance: List<Performance>
)
