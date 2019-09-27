package com.croczi.teami.models

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

class MyResources(
    @SerializedName("resource_type")
    var resourceType:String,
    var id: Int,
    var name: String,
    var street: String,
    @SerializedName("work_time")
    var workTime: String?,
    var organisation: String,
    var speciality: String,
    var hospital: String?,
    var reign: String,
    var latitude: String,
    var longitude: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(resourceType)
        parcel.writeInt(id)
        parcel.writeString(name)
        parcel.writeString(street)
        parcel.writeString(workTime)
        parcel.writeString(organisation)
        parcel.writeString(speciality)
        parcel.writeString(hospital)
        parcel.writeString(reign)
        parcel.writeString(latitude)
        parcel.writeString(longitude)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MyResources> {
        override fun createFromParcel(parcel: Parcel): MyResources {
            return MyResources(parcel)
        }

        override fun newArray(size: Int): Array<MyResources?> {
            return arrayOfNulls(size)
        }
    }
}