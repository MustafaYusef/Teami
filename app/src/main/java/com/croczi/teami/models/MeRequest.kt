package com.croczi.teami.models

import com.google.gson.annotations.SerializedName

class MeRequest(
    var token: String?,
    @SerializedName("phone_id")
    var phoneId: String
)