package com.croczi.teami.retrofit

import com.croczi.teami.models.*
import retrofit2.Call
import retrofit2.http.*


interface RepresentativesInterface {

    @GET("get-items")
    fun getItems(
        @Query("token") token: String,
        @Query("phone_id") phoneId: String
    ): Call<ItemsResponse>

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
    fun getSpecialty(
        @Query("token") token: String,
        @Query("phone_id") phoneId: String
    ): Call<SpecialityResponse>

    @GET("GetReigns")
    fun getRegion(
        @Query("token") token: String,
        @Query("phone_id") phoneId: String,
        @Query("organisation_id") orgId: Int
    ): Call<RegionResponse>

    @GET("get-status")
    fun getStatus(
        @Query("token") token: String,
        @Query("phone_id") phoneId: String
    ): Call<StatusResponse>

    @GET("GetPharmacyStatus")
    fun getPharmStatus(
        @Query("token") token: String,
        @Query("phone_id") phoneId: String
    ): Call<PharmStatusResponse>

    @GET("GetOrganisations")
    fun getOrgs(
        @Query("token") token: String,
        @Query("phone_id") phoneId: String
    ): Call<OrganizationResponse>

    @GET("GetHospitals")
    fun getHospitals(
        @Query("token") token: String,
        @Query("phone_id") phoneId: String,
        @Query("organisation_id") orgId: Int
    ): Call<HospitalsResponse>

    @POST("add-doctor")
    fun addNewDoctor(@Body doctor: Doctor): Call<AddDoctorResponse>

    @POST("add-pharmacy")
    fun addNewPharmacy(@Body pharmacy: Pharmacy): Call<AddPharmacyResponse>

    @GET("GetMyResource")
    fun getMyResources(
        @Query("token") token: String?,
        @Query("phone_id") phoneId: String
    ): Call<MyResourcesResponse>

    @POST("post-feedback")
    fun postFeedback(@Body feedbackRequest: FeedbackRequest): Call<FeedbackResponse>

    @POST("post-order")
    fun postOrder(@Body orderRequest: OrderRequest): Call<OrderResponse>

    @GET("GetHistory")
    fun getHistory(
        @Query("token") token: String,
        @Query("phone_id") phoneId: String
    ): Call<HistoryResponse>

    @GET("GetUserPerformance")
    fun getUserPerformance(
        @Query("token") token: String,
        @Query("phone_id") phoneId: String
    ): Call<PerformanceResponse>

    @GET("CheckUserOrders")
    fun checkUserOrders(
        @Query("token") token: String,
        @Query("phone_id") phoneId: String
    ): Call<UserOrderResponse>

}