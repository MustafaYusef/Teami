package com.croczi.teami.models

import com.google.gson.annotations.SerializedName

class UserOrder(
    @SerializedName("count_of_itemType")
    var countOfItemType: Int,
    @SerializedName("count_of_item")
    var countOfItem: Int,
    var pharmacyName: String
)