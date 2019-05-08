package com.croczi.teami.models

import com.google.gson.annotations.SerializedName

class LogoutRequest(
    var token: String,
    @SerializedName("phone_id")
    var phoneId: String
)