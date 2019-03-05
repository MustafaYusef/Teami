package com.martin.teami.retrofit

import com.martin.teami.models.*
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST


interface RepresentativesInterface {

    @GET(".")
    fun getBooks(): Call<List<Item>>

    @GET("pharmacies/")
    fun getPharmacies(): Call<PharmaciesResponse>

    @POST("pharmacies/edit/")
    fun registerPharmacy(
        @Header("Content-Type") header1: String,
        @Header("cache-control") header2: String,
        @Header("crossDomain") header3: Boolean,
        @Body pharmacy: Pharmacy
    ): Call<RegisterResponse>

    @POST("login")
    fun getToken(@Body loginRequest: LoginRequest): Call<LoginResponse>

    @POST("check")
    fun checkToken(@Body checkRequest: CheckRequest): Call<CheckResponse>

    @POST("refresh")
    fun getRefresh(@Body loginRequest: LoginRequest): Call<LoginResponse>

    @POST("logout")
    fun logout(@Body logoutRequest: LogoutRequest): Call<LogoutResponse>

    @POST("me")
    fun getMe(@Body meRequest: MeRequest): Call<MeResponse>
}