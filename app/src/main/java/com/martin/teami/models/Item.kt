package com.martin.teami.models

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

class Item(
    var id: Int,
    var name: String,
    var description: String,
    @SerializedName("company_name")
    var companyName: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(name)
        parcel.writeString(description)
        parcel.writeString(companyName)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Item> {
        override fun createFromParcel(parcel: Parcel): Item {
            return Item(parcel)
        }

        override fun newArray(size: Int): Array<Item?> {
            return arrayOfNulls(size)
        }
    }

    override fun toString(): String {
        return "$name : $companyName"
    }
}