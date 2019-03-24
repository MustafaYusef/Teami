package com.martin.teami.models

class OrderRequest(
var token:String,
var phone_id:String,
var activity_id:String,
var items:List<ItemsOrdered>,
var resource_type:String,
var resource_id:Int
)