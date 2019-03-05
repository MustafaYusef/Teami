package com.martin.teami.models

import com.google.gson.annotations.SerializedName

class LoginRequest(var email: String,
                   var password: String,
                   @SerializedName("phone_id")
                   var phoneId: String)