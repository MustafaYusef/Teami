package com.martin.teami.models

import com.google.gson.annotations.SerializedName

class ActivityRequest(
    var token:String,
    @SerializedName("resource_type")
    var resourceType: String?,
    @SerializedName("resource_id")
    var resourceId: String?,
    @SerializedName("status_id")
    var statusId: String?,
    var note: String?,
    @SerializedName("activity_type")
    var activityType: String?
)