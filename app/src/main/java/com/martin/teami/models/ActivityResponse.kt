package com.martin.teami.models

import com.google.gson.annotations.SerializedName

class ActivityResponse(
    @SerializedName("resource_type")
    var resourceType: String?,
    @SerializedName("resource_id")
    var resourceId: String?,
    @SerializedName("status_id")
    var statusId: String?,
    var note: String?,
    @SerializedName("activity_type")
    var activityType: String?,
    @SerializedName("user_id")
    var userId: Int?,
    @SerializedName("role_id")
    var roleId: Int?,
    @SerializedName("phone_id")
    var phoneId: String?,
    @SerializedName("updated_at")
    var updatedAt: String?,
    @SerializedName("created_at")
    var createdAt: String?,
    var id: Int?
)