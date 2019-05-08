package com.croczi.teami.models
import com.google.gson.annotations.SerializedName


class HistoryResponse(
    @SerializedName("History")
    var history: List<History>
)
