package com.croczi.teami.models

import android.os.Parcel
import android.os.Parcelable

class CoverageArea(
    val name:String
) : Parcelable {
    constructor(parcel: Parcel) : this(parcel.readString()!!) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CoverageArea> {
        override fun createFromParcel(parcel: Parcel): CoverageArea {
            return CoverageArea(parcel)
        }

        override fun newArray(size: Int): Array<CoverageArea?> {
            return arrayOfNulls(size)
        }
    }
}