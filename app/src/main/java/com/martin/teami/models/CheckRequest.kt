package com.martin.teami.models

import com.google.gson.annotations.SerializedName

class CheckRequest(
    private var token: String,
    @SerializedName("phone_id")
    private var phoneId: String
)