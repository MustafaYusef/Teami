package com.croczi.teami.database.entitis

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
@Entity
data class ItemLocal(
    @PrimaryKey(autoGenerate = true)
    var IdDb:Int=0,
    var id: Int=0,
    var name: String="",
    var description: String="",
    var companyName: String=""
)
