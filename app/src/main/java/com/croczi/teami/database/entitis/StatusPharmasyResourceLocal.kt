package com.croczi.teami.database.entitis

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity
data class StatusPharmasyResourceLocal(
    @PrimaryKey(autoGenerate = true)
    var IdDb:Int=0,
    var id:Int,
    var text:String
)


