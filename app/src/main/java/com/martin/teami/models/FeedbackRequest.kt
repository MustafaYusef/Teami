package com.martin.teami.models

import com.google.gson.annotations.SerializedName


class FeedbackRequest(
    var token:String,
    @SerializedName("phone_id")
    var phoneId:String,
    @SerializedName("resource_type")
    var resourceType: String?,
    @SerializedName("resource_id")
    var resourceId: String?,
    @SerializedName("status_id")
    var statusId: String?,
    var note: String?,
    @SerializedName("activity_type")
    var activityType: String?,
    @SerializedName("reminders_products")
    var remindersProducts:String?,
    @SerializedName("call_products")
    var callProducts:String?
)
