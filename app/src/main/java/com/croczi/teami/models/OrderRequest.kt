package com.croczi.teami.models

class OrderRequest(
var token:String,
var phone_id:String,
var item_id:List<Int>,
var quantity:List<String>,
var resource_type:String,
var resource_id:Int,
var Insert_order:String?
)