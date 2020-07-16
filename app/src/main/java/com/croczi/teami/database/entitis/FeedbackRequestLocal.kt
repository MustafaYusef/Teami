package com.croczi.teami.database.entitis

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class FeedbackRequestLocal(
    @PrimaryKey(autoGenerate = true)
    var IdDb:Int=0,
    var token:String,
    var phoneId:String,
    var resourceType: String?,
    var resourceId: String?,
    var statusId: String?,
    var note: String?,
    var activityType: String?,
    var remindersProducts:String?,
    var callProducts:String?
)
