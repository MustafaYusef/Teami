package com.martin.teami.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.AsyncTask
import android.provider.Settings
import android.widget.Toast
import com.martin.teami.activities.LoginActivity
import com.martin.teami.models.*
import com.martin.teami.retrofit.RepresentativesInterface
import com.martin.teami.utils.Consts.BASE_URL
import com.martin.teami.utils.Consts.LOGIN_RESPONSE_SHARED
import com.orhanobut.hawk.Hawk
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import java.util.*

private lateinit var token: String
private var tokenExp: Long = 0
private var calendar: Calendar? = null

fun checkUser(context: Activity):LoginResponse? {
    val loginResponse = Hawk.get<LoginResponse>(LOGIN_RESPONSE_SHARED)
    calendar = Hawk.get(Consts.LOGIN_TIME)
    if (loginResponse != null) {
        token = loginResponse.token
        tokenExp = loginResponse.expire
        checkExpirationLimit(token, tokenExp, getID(context), calendar, context)
        return loginResponse
    } else {
        val intent = Intent(context, LoginActivity::class.java)
        Hawk.deleteAll()
        intent.flags = Intent
            .FLAG_ACTIVITY_CLEAR_TOP or Intent
            .FLAG_ACTIVITY_NO_HISTORY or Intent
            .FLAG_ACTIVITY_NEW_TASK or Intent
            .FLAG_ACTIVITY_CLEAR_TASK
        context.finish()
        context.startActivity(intent)
        return null
    }
}

fun checkExpirationLimit(token: String, tokenSecs: Long, phoneId: String, calendar: Calendar?, activity: Activity) {
    val currentCalendar = Calendar.getInstance(TimeZone.getDefault())
    calendar?.let {
        val loginSecs = calendar.timeInMillis / 1000
        val currentSecs = currentCalendar.timeInMillis / 1000
        val difference = currentSecs - loginSecs
        if (tokenSecs - (0.17 * tokenSecs) > difference)
            checkTokenWithBackEnd(activity, token, phoneId)
        else
            getRefresh(activity, token, phoneId)
//            logoutUser(activity, token,phoneId)
    }
}

fun checkTokenWithBackEnd(activity: Activity, token: String, phoneId: String) {
    val retrofit = Retrofit.Builder()
        .baseUrl(Consts.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val checkInterface = retrofit.create(RepresentativesInterface::class.java)
    val checkRequest = CheckRequest(token, phoneId)
    val checkResponseCall = checkInterface.checkToken(checkRequest)
    checkResponseCall.enqueue(object : Callback<CheckResponse> {
        override fun onFailure(call: Call<CheckResponse>, t: Throwable) {
            Toast.makeText(activity, t.message, Toast.LENGTH_LONG).show()
            getRefresh(activity, token, phoneId)
//            logoutUser(activity, token,phoneId)
        }

        override fun onResponse(call: Call<CheckResponse>, response: Response<CheckResponse>) {
            val checkResponse = response.body()
            if (checkResponse?.success != "Token valid")
                getRefresh(activity, token, phoneId)
//                logoutUser(activity, token,phoneId)
        }
    })
}

fun getRefresh(activity: Activity, token: String, phoneId: String) {
    val retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(BASE_URL)
        .build()
    val refreshInterface = retrofit.create(RepresentativesInterface::class.java)
    val refreshRequest = RefreshRequest(token, phoneId)
    val refreshResponseCall = refreshInterface.getRefresh(refreshRequest).enqueue(object : Callback<LoginResponse> {
        override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
            Toast.makeText(activity, t.message, Toast.LENGTH_LONG).show()
        }

        override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
            val refreshResponse = response.body()
            if (refreshResponse?.token != null)
                Hawk.put(LOGIN_RESPONSE_SHARED, refreshResponse)
            else logoutUser(activity, token, phoneId)
        }
    })
}

fun logoutUser(activity: Activity, token: String, phoneId: String) {
    Hawk.deleteAll()
    val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val logoutInterface = retrofit.create(RepresentativesInterface::class.java)
    val logoutRequest = LogoutRequest(token, phoneId)
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

fun getID(context: Context?): String {
    return Settings.Secure.getString(context?.contentResolver, Settings.Secure.ANDROID_ID)
}
