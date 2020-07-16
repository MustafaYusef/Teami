package com.croczi.teami.retrofit

import com.croczi.teami.models.AppLockedResponse
import com.croczi.teami.models.MockResponse
import com.croczi.teami.models.MockUser
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ToolsInterface {

//    @GET(".")
//    fun getIfAppIsLocked(): Call<AppLockedResponse>
//
    @POST(".")
    fun postMockUser(
        @Body mockUser: MockUser
    ): Call<MockResponse>

}