package com.croczi.teami.models
import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName


class MeResponse(
    @SerializedName("user")
    var user: User
) : Parcelable {
    constructor(parcel: Parcel) : this(parcel.readParcelable<User>(User::class.java.classLoader)!!)

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(user, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MeResponse> {
        override fun createFromParcel(parcel: Parcel): MeResponse {
            return MeResponse(parcel)
        }

        override fun newArray(size: Int): Array<MeResponse?> {
            return arrayOfNulls(size)
        }
    }
}
