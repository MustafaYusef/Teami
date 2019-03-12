package com.martin.teami.models

class OrderRequest(
var token:String,
var phone_id:String,
var activity_id:String,
var items: List<Item>
)