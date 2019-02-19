package com.martin.representativesmap.utils

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import com.martin.representativesmap.activities.LoginActivity
import com.martin.representativesmap.models.CheckRequest
import com.martin.representativesmap.models.CheckResponse
import com.martin.representativesmap.models.LogoutRequest
import com.martin.representativesmap.models.LogoutResponse
import com.martin.representativesmap.retrofit.RepresentativesInterface
import com.orhanobut.hawk.Hawk
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*

fun checkExpirationLimit(token: String, tokenSecs: Int, calendar: Calendar?, activity: Activity) {
    val currentCalendar = Calendar.getInstance(TimeZone.getDefault())
    calendar?.let {
        val loginSecs = calendar.timeInMillis / 1000
        val currentSecs = currentCalendar.timeInMillis / 1000
        val difference = currentSecs - loginSecs
        if (difference < tokenSecs)
            checkTokenWithBackEnd(activity, token)
        else logoutUser(activity, token)
    }
}

fun checkTokenWithBackEnd(activity: Activity, token: String) {
    val retrofit = Retrofit.Builder()
        .baseUrl(Consts.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val checkInterface = retrofit.create(RepresentativesInterface::class.java)
    val checkRequest = CheckRequest(token)
    val checkResponseCall = checkInterface.checkToken(checkRequest)
    checkResponseCall.enqueue(object : Callback<CheckResponse> {
        override fun onFailure(call: Call<CheckResponse>, t: Throwable) {
            Toast.makeText(activity, t.message, Toast.LENGTH_LONG).show()
            logoutUser(activity, token)
        }

        override fun onResponse(call: Call<CheckResponse>, response: Response<CheckResponse>) {
            val checkResponse = response.body()
            if (checkResponse?.success != "Token valid")
                logoutUser(activity, token)
        }
    })
}

fun logoutUser(activity: Activity, token: String) {
    Hawk.delete(Consts.LOGIN_RESPONSE_SHARED)
    val retrofit = Retrofit.Builder()
        .baseUrl(Consts.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val logoutInterface = retrofit.create(RepresentativesInterface::class.java)
    val logoutRequest = LogoutRequest(token)
    val logoutResponseCall = logoutInterface.logout(logoutRequest)
    logoutResponseCall.enqueue(object : Callback<LogoutResponse> {
        override fun onFailure(call: Call<LogoutResponse>, t: Throwable) {
            Toast.makeText(activity, t.message, Toast.LENGTH_LONG).show()
        }

        override fun onResponse(call: Call<LogoutResponse>, response: Response<LogoutResponse>) {
            val logoutResponse = response.body()
            val intent = Intent(activity, LoginActivity::class.java)
            activity.startActivity(intent)
            activity.finish()
        }
    })
}
