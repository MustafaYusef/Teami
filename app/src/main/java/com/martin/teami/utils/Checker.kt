package com.martin.teami.utils

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import com.martin.teami.activities.LoginActivity
import com.martin.teami.models.CheckRequest
import com.martin.teami.models.CheckResponse
import com.martin.teami.models.LogoutRequest
import com.martin.teami.models.LogoutResponse
import com.martin.teami.retrofit.RepresentativesInterface
import com.orhanobut.hawk.Hawk
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*

fun checkExpirationLimit(token: String, tokenSecs: Int,phoneId:String, calendar: Calendar?, activity: Activity) {
    val currentCalendar = Calendar.getInstance(TimeZone.getDefault())
    calendar?.let {
        val loginSecs = calendar.timeInMillis / 1000
        val currentSecs = currentCalendar.timeInMillis / 1000
        val difference = currentSecs - loginSecs
        if (difference < tokenSecs)
            checkTokenWithBackEnd(activity, token,phoneId)
        else logoutUser(activity, token,phoneId)
    }
}

fun checkTokenWithBackEnd(activity: Activity, token: String,phoneId: String) {
    val retrofit = Retrofit.Builder()
        .baseUrl(Consts.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val checkInterface = retrofit.create(RepresentativesInterface::class.java)
    val checkRequest = CheckRequest(token,phoneId)
    val checkResponseCall = checkInterface.checkToken(checkRequest)
    checkResponseCall.enqueue(object : Callback<CheckResponse> {
        override fun onFailure(call: Call<CheckResponse>, t: Throwable) {
            Toast.makeText(activity, t.message, Toast.LENGTH_LONG).show()
            logoutUser(activity, token,phoneId)
        }

        override fun onResponse(call: Call<CheckResponse>, response: Response<CheckResponse>) {
            val checkResponse = response.body()
            if (checkResponse?.success != "Token valid")
                logoutUser(activity, token,phoneId)
        }
    })
}

fun logoutUser(activity: Activity, token: String,phoneId: String) {
    Hawk.deleteAll()
    val retrofit = Retrofit.Builder()
        .baseUrl(Consts.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val logoutInterface = retrofit.create(RepresentativesInterface::class.java)
    val logoutRequest = LogoutRequest(token,phoneId)
    val logoutResponseCall = logoutInterface.logout(logoutRequest)
    logoutResponseCall.enqueue(object : Callback<LogoutResponse> {
        override fun onFailure(call: Call<LogoutResponse>, t: Throwable) {
            Toast.makeText(activity, t.message, Toast.LENGTH_LONG).show()
        }

        override fun onResponse(call: Call<LogoutResponse>, response: Response<LogoutResponse>) {
            val logoutResponse = response.body()
            val intent = Intent(activity, LoginActivity::class.java)
            intent.flags = Intent
                .FLAG_ACTIVITY_CLEAR_TOP or Intent
                .FLAG_ACTIVITY_NO_HISTORY or Intent
                .FLAG_ACTIVITY_NEW_TASK or Intent
                .FLAG_ACTIVITY_CLEAR_TASK
            activity.finish()
            activity.startActivity(intent)
        }
    })
}
