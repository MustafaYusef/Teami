package com.martin.teami.models

class Resource(
    var id:Int,
    var text:String
){
    override fun toString(): String {
        return text
    }
}