package com.croczi.teami.models

import android.os.Parcel
import android.os.Parcelable

class ItemsOrdered(var item_id: Int,
                   var quantity:Float) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readFloat()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(item_id)
        parcel.writeFloat(quantity)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ItemsOrdered> {
        override fun createFromParcel(parcel: Parcel): ItemsOrdered {
            return ItemsOrdered(parcel)
        }

        override fun newArray(size: Int): Array<ItemsOrdered?> {
            return arrayOfNulls(size)
        }
    }
}
