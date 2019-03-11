package com.martin.teami.models


class User(
    val UserName: String,
    val Phone: String,
    val Email: String,
    val Role: String,
    val Reporting_to: String,
    val Coverage_Area: MutableList<CoverageArea>,
    val Resources: List<String>,
    val Userid: Int
)