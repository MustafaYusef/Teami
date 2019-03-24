package com.martin.teami.models
import com.google.gson.annotations.SerializedName

class OrderResponse(
    @SerializedName("order")
    var order: List<Order>
)
