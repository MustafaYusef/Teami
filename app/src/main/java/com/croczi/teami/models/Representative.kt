package com.croczi.teami.models

class Representative(
    var id:Int,
    var name: String,
    var userName:String,
    var phone: Long,
    var email: String,
    var area: String,
    var pharmacies: List<Pharmacy>,
    var doctors:List<Doctor>,
    var drugStores:List<Drugstore>,
    var babyShops:List<BabyShop>,
    var beautyCenters:List<BeautyCenter>,
    var token: String,
    var deviceID:String
)