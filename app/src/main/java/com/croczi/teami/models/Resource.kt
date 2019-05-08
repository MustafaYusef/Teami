package com.croczi.teami.models

import android.os.Parcel
import android.os.Parcelable

class Resource(var id:Int, var name:String) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString()
    ) {
    }

    override fun toString(): String {
        return name
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(name)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Resource> {
        override fun createFromParcel(parcel: Parcel): Resource {
            return Resource(parcel)
        }

        override fun newArray(size: Int): Array<Resource?> {
            return arrayOfNulls(size)
        }
    }
}