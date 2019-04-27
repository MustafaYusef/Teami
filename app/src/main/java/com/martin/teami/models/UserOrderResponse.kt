package com.martin.teami.models
import com.google.gson.annotations.SerializedName


class UserOrderResponse(
    @SerializedName("UserOrders")
    var userOrders: List<UserOrder>
)
