package com.croczi.teami.database.entitis

import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity
data class OrderRequestLocal(
    @PrimaryKey(autoGenerate = true)
    var IdDb:Int=0,
var token:String,
var phone_id:String,
var item_idArray:String,
var quantityArray:String,
var resource_type:String,
var resource_id:Int
)