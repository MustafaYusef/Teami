package com.mustafayusef.sharay.database.entitis


import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable


@Entity
class MyResourcesLocal(
    @PrimaryKey(autoGenerate = true)
    var idDb:Int=0,
    var resourceType:String,
    var id: Int,
    var name: String,
    var street: String,

    var workTime: String?,
    var organisation: String,
    var speciality: String,
    var hospital: String?,
    var reign: String,
    var latitude: String,
    var longitude: String
):Serializable