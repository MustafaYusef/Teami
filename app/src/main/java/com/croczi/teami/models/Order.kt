package com.croczi.teami.models

import com.google.gson.annotations.SerializedName

class Order(
    @SerializedName("user_id")
    var userId: Int,
    @SerializedName("activity_id")
    var activityId: Int,
    @SerializedName("item_id")
    var itemId: String,
    @SerializedName("quantity")
    var quantity: String,
    @SerializedName("updated_at")
    var updatedAt: String,
    @SerializedName("created_at")
    var createdAt: String,
    var id: Int
)