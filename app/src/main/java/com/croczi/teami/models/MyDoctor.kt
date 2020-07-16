package com.croczi.teami.models
import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

class MyDoctor(
    @SerializedName("id")
    var id: Int,
    @SerializedName("name")
    var name: String,
    @SerializedName("work_time")
    var workTime: String,
    @SerializedName("street")
    var street: String,
    @SerializedName("latitude")
    var latitude: String,
    @SerializedName("longitude")
    var longitude: String,
    @SerializedName("speciality_id")
    var specialityId: Int,
    @SerializedName("reign_id")
    var reignId: Int,
    @SerializedName("organisation_id")
    var organisationId: Int,
    @SerializedName("hospital_id")
    var hospitalId: Int,
    @SerializedName("speciality")
    var speciality: Resource,
    @SerializedName("reign")
    var reign: Resource,
    @SerializedName("organisation")
    var organisation: Resource,
    @SerializedName("hospital")
    var hospital: Resource
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readParcelable(Resource::class.java.classLoader)!!,
        parcel.readParcelable(Resource::class.java.classLoader)!!,
        parcel.readParcelable(Resource::class.java.classLoader)!!,
        parcel.readParcelable(Resource::class.java.classLoader)!!
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(name)
        parcel.writeString(workTime)
        parcel.writeString(street)
        parcel.writeString(latitude)
        parcel.writeString(longitude)
        parcel.writeInt(specialityId)
        parcel.writeInt(reignId)
        parcel.writeInt(organisationId)
        parcel.writeInt(hospitalId)
        parcel.writeParcelable(speciality, flags)
        parcel.writeParcelable(reign, flags)
        parcel.writeParcelable(organisation, flags)
        parcel.writeParcelable(hospital, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MyDoctor> {
        override fun createFromParcel(parcel: Parcel): MyDoctor {
            return MyDoctor(parcel)
        }

        override fun newArray(size: Int): Array<MyDoctor?> {
            return arrayOfNulls(size)
        }
    }
}