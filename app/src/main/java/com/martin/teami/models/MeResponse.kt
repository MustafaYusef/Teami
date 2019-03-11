package com.martin.teami.models
import com.google.gson.annotations.SerializedName


class MeResponse(
    @SerializedName("user")
    var user: User
)
