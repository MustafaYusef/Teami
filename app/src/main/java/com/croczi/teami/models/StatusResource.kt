package com.croczi.teami.models

import android.os.Parcel
import android.os.Parcelable

class StatusResource(var id:Int, var text:String) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString()!!
    )
    override fun toString(): String {
        return text
    }
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(text)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<StatusResource> {
        override fun createFromParcel(parcel: Parcel): StatusResource {
            return StatusResource(parcel)
        }

        override fun newArray(size: Int): Array<StatusResource?> {
            return arrayOfNulls(size)
        }
    }
}
