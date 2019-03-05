package com.martin.teami.models
import com.google.gson.annotations.SerializedName


data class MeResponse(
    @SerializedName("user")
    var user: User = User()
)

data class User(
    @SerializedName("id")
    var id: Int = 0,
    @SerializedName("name")
    var name: String = "",
    @SerializedName("email")
    var email: String = "",
    @SerializedName("email_verified_at")
    var emailVerifiedAt: Any = Any(),
    @SerializedName("phone_id")
    var phoneId: String="",
    @SerializedName("phone")
    var phone: Any = Any(),
    @SerializedName("created_by")
    var createdBy: Any = Any(),
    @SerializedName("child_num")
    var childNum: Any = Any(),
    @SerializedName("first_login")
    var firstLogin: Int = 0,
    @SerializedName("deleted_at")
    var deletedAt: Any = Any(),
    @SerializedName("created_at")
    var createdAt: String = "",
    @SerializedName("updated_at")
    var updatedAt: String = ""
)