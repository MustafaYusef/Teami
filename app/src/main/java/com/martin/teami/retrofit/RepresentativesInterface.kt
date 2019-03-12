package com.martin.teami.retrofit

import com.martin.teami.models.*
import retrofit2.Call
import retrofit2.http.*


interface RepresentativesInterface {

    @GET(".")
    fun getBooks(): Call<List<Item>>

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
    fun getRefresh(@Body refreshRequest: RefreshRequest): Call<LoginResponse>

    @POST("logout")
    fun logout(@Body logoutRequest: LogoutRequest): Call<LogoutResponse>

    @POST("me")
    fun getMe(@Body meRequest: MeRequest): Call<MeResponse>

    @POST("forget-password")
    fun sendForgotPasswordEmail(@Body forgotRequest: ForgotRequest): Call<ForgotResponse>

    @GET("GetSpecialities")
    fun getSpecialty(@Query("token") token:String,
                     @Query("phone_id") phoneId:String):Call<SpecialityResponse>

    @GET("GetReigns")
    fun getRegion(@Query("token") token:String,
                  @Query("phone_id") phoneId:String,
                  @Query("organisation_id")orgId:Int):Call<RegionResponse>

    @GET("get-status")
    fun getStatus(@Query("token") token:String,
                  @Query("phone_id") phoneId:String):Call<StatusResponse>
    @GET("GetOrganisations")
    fun getOrgs(@Query("token") token:String,
                @Query("phone_id") phoneId:String):Call<OrganizationResponse>

    @GET("GetHospitals")
    fun getHospitals(@Query("token") token:String,
                @Query("phone_id") phoneId:String):Call<HospitalsResponse>

    @POST("AddNewDoctor")
    fun addNewDoctor(@Body doctor: Doctor):Call<AddDoctorResponse>

    @POST("AddNewPharmacy")
    fun addNewPharmacy(@Body pharmacy: Pharmacy):Call<AddPharmacyResponse>

    @GET("GetMyResource")
    fun getMyResources(@Query("token") token: String,
                       @Query("phone_id") phoneId: String):Call<MyResourcesResponse>

    @POST("post-feedback")
    fun postFeedback(@Body feedbackRequest: FeedbackRequest):Call<FeedbackResponse>

    @POST("post-order")
    fun postOrder(@Body orderRequest:OrderRequest ):Call<FeedbackResponse>
}