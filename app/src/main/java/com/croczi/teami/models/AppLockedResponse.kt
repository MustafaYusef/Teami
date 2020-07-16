package com.croczi.teami.models

import android.os.Parcel
import android.os.Parcelable

class AppLockedResponse (
    var isLocked:Boolean=false,
    var imgUrl:String="",
    var title:String="",
    var msg:String=""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readByte() != 0.toByte(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByte(if (isLocked) 1 else 0)
        parcel.writeString(imgUrl)
        parcel.writeString(title)
        parcel.writeString(msg)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AppLockedResponse> {
        override fun createFromParcel(parcel: Parcel): AppLockedResponse {
            return AppLockedResponse(parcel)
        }

        override fun newArray(size: Int): Array<AppLockedResponse?> {
            return arrayOfNulls(size)
        }
    }
}