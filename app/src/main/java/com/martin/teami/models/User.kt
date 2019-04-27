package com.martin.teami.models

import android.os.Parcel
import android.os.Parcelable


class User(
    val UserName: String,
    val Phone: String,
    val Email: String,
    val Role: String,
    val Reporting_to: String,
    val Coverage_Area: List<CoverageArea>,
    val Resources: List<String>,
    val Userid: Int
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.createTypedArrayList(CoverageArea),
        parcel.createStringArrayList(),
        parcel.readInt()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(UserName)
        parcel.writeString(Phone)
        parcel.writeString(Email)
        parcel.writeString(Role)
        parcel.writeString(Reporting_to)
        parcel.writeTypedList(Coverage_Area)
        parcel.writeStringList(Resources)
        parcel.writeInt(Userid)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel): User {
            return User(parcel)
        }

        override fun newArray(size: Int): Array<User?> {
            return arrayOfNulls(size)
        }
    }
}