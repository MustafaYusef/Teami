package com.croczi.teami.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Build
import android.provider.Settings
import com.croczi.teami.models.*
import com.croczi.teami.retrofit.RepresentativesInterface
import com.croczi.teami.retrofit.addTLSSupport
import com.croczi.teami.utils.Consts.BASE_URL
import com.croczi.teami.utils.Consts.LOGIN_RESPONSE_SHARED
import com.croczi.teami.utils.Consts.LOGIN_TIME
import com.orhanobut.hawk.Hawk
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*

private lateinit var token: String
private var tokenExp: Long = 0
private var calendar: Calendar? = null

fun checkUser(context: Activity, response: (status: UserStatus,loginResponse:LoginResponse?) -> Unit) {
    val loginResponse = Hawk.get<LoginResponse>(LOGIN_RESPONSE_SHARED)
    calendar = Hawk.get(LOGIN_TIME)
    if (loginResponse != null) {
        token = loginResponse.token
        tokenExp = loginResponse.expire
        checkTokenWithBackEnd(getID(context), token, tokenExp, calendar) { checkWithBackendStatus ->
            when (checkWithBackendStatus) {
                CheckWithBackendStatus.NetworkError -> response(UserStatus.NetworkError,null)
                CheckWithBackendStatus.TokenNotValid -> logoutUser(token, getID(context)) { logoutStatus ->
                    response(logoutStatus,null)
                }
                CheckWithBackendStatus.TokenValid -> checkIfShouldRefresh(tokenExp, calendar) { shouldRefreshStatus ->
                    when (shouldRefreshStatus) {
                        ShouldRefreshStatus.ShouldRefresh -> {
                            getRefresh(token, getID(context)) { refreshStatus, refreshResponse ->
                                when (refreshStatus) {
                                    RefreshStatus.RefreshAcquired -> {
                                        Hawk.put(LOGIN_RESPONSE_SHARED, refreshResponse)
                                        response(UserStatus.LoggedIn,refreshResponse)
                                    }
                                    RefreshStatus.RefreshNotAcquired -> logoutUser(token, getID(context)) {
                                        response(it,null)
                                    }
                                    RefreshStatus.NetworkError -> response(UserStatus.NetworkError,null)
                                }
                            }
                        }
                        ShouldRefreshStatus.ShouldNotRefresh -> response(UserStatus.LoggedIn,loginResponse)
                    }
                }
            }

        }
//        checkIfShouldRefresh(token, tokenExp, getID(context), calendar) {
//            response(it)
//        }
//            return loginResponse
    }
//        context is MainActivity -> return null
    else {
        response(UserStatus.LoggedOut,null)
//            val intent = Intent(context, MainActivity::class.java)
//            Hawk.deleteAll()
//            intent.flags = Intent
//                .FLAG_ACTIVITY_CLEAR_TOP or Intent
//                .FLAG_ACTIVITY_NO_HISTORY or Intent
//                .FLAG_ACTIVITY_NEW_TASK or Intent
//                .FLAG_ACTIVITY_CLEAR_TASK
//            context.finish()
//            context.startActivity(intent)
//            return null
    }
}


fun checkTokenWithBackEnd(
    phoneId: String,
    token: String,
    tokenExp: Long,
    calendar: Calendar?,
    response: (status: CheckWithBackendStatus) -> Unit
) {
    var retrofitBuilder = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
        retrofitBuilder.addTLSSupport()
    }
    val retrofit = retrofitBuilder.build()
    val checkInterface = retrofit.create(RepresentativesInterface::class.java)
    val checkRequest = CheckRequest(token, phoneId)
    val checkResponseCall = checkInterface.checkToken(checkRequest)
    checkResponseCall.enqueue(object : Callback<CheckResponse> {
        override fun onFailure(call: Call<CheckResponse>, t: Throwable) {
            response(CheckWithBackendStatus.NetworkError)
//            Toast.makeText(activity, t.message, Toast.LENGTH_LONG).show()
//            if (activity is MainActivity) {
//                activity.gotoMain()
//            }
//            getRefresh(activity, token, phoneId)
//            logoutUser(activity, token,phoneId)
        }

        override fun onResponse(call: Call<CheckResponse>, response: Response<CheckResponse>) {
            val checkResponse = response.body()
            if (checkResponse?.success != "Token valid") {
//                getRefresh(activity, token, phoneId) {
                response(CheckWithBackendStatus.TokenNotValid)
//                }
            } else response(CheckWithBackendStatus.TokenValid)
//            else if (activity is MainActivity)

//                activity.gotoMain()
////                logoutUser(activity, token,phoneId)
        }
    })
}

fun checkIfShouldRefresh(
    tokenLimit: Long,
    calendar: Calendar?,
    response: (status: ShouldRefreshStatus) -> Unit
) {
    val currentCalendar = Calendar.getInstance(TimeZone.getDefault())
    calendar?.let {
        val loginSecs = calendar.timeInMillis / 1000
        val currentSecs = currentCalendar.timeInMillis / 1000
        val timeSinceLogged = currentSecs - loginSecs
        if (timeSinceLogged < tokenLimit / 2)
            response(ShouldRefreshStatus.ShouldNotRefresh)
        else response(ShouldRefreshStatus.ShouldRefresh)
    }
}

fun getRefresh(
    token: String,
    phoneId: String,
    response: (status: RefreshStatus, refreshResponse: LoginResponse?) -> Unit
) {
    var retrofitBuilder = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
        retrofitBuilder.addTLSSupport()
    }
    val retrofit = retrofitBuilder.build()
    val refreshInterface = retrofit.create(RepresentativesInterface::class.java)
    val refreshRequest = RefreshRequest(token, phoneId)
    val refreshResponseCall = refreshInterface.getRefresh(refreshRequest).enqueue(object : Callback<LoginResponse> {
        override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
            response(RefreshStatus.NetworkError, null)
//            Toast.makeText(activity, t.message, Toast.LENGTH_LONG).show()
//            if (activity is MainActivity) {
//                activity.gotoMain()
//            }
        }

        override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
            val refreshResponse = response.body()
            if (refreshResponse?.token != null) {
                response(RefreshStatus.RefreshAcquired, refreshResponse)
//                if (activity is MainActivity)
//                    activity.gotoMain()
            } else response(RefreshStatus.RefreshNotAcquired, null)
        }
    })
}

fun logoutUser(token: String?, phoneId: String, response: (status: UserStatus) -> Unit) {
    val retrofitBuilder = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
        retrofitBuilder.addTLSSupport()
    }
    val retrofit = retrofitBuilder.build()
    val logoutInterface = retrofit.create(RepresentativesInterface::class.java)
    val logoutRequest = LogoutRequest(token, phoneId)
    val logoutResponseCall = logoutInterface.logout(logoutRequest)
    logoutResponseCall.enqueue(object : Callback<LogoutResponse> {
        override fun onFailure(call: Call<LogoutResponse>, t: Throwable) {
            response(UserStatus.NetworkError)
//            Toast.makeText(activity, t.message, Toast.LENGTH_LONG).show()
//            if (activity is MainActivity) {
//                activity.gotoMain()
//            }
        }

        override fun onResponse(call: Call<LogoutResponse>, response: Response<LogoutResponse>) {
            response(UserStatus.LoggedOut)
//            if (activity is MainActivity) {
//                activity.gotoLogin()
//            } else {
//                val intent = Intent(activity, MainActivity::class.java)
//                intent.flags = Intent
//                    .FLAG_ACTIVITY_CLEAR_TOP or Intent
//                    .FLAG_ACTIVITY_NO_HISTORY or Intent
//                    .FLAG_ACTIVITY_NEW_TASK or Intent
//                    .FLAG_ACTIVITY_CLEAR_TASK
//                activity.finish()
//                intent.putExtra("logout",false)
//                activity.startActivity(intent)
//            }
        }
    })
}

@SuppressLint("HardwareIds")
fun getID(context: Context?): String {
    return Settings.Secure.getString(context?.contentResolver, Settings.Secure.ANDROID_ID)
}

enum class UserStatus {
    LoggedIn,
    LoggedOut,
    NetworkError
}

enum class RefreshStatus {
    RefreshAcquired,
    NetworkError,
    RefreshNotAcquired
}

enum class CheckWithBackendStatus {
    TokenNotValid,
    TokenValid,
    NetworkError
}

enum class ShouldRefreshStatus {
    ShouldNotRefresh,
    ShouldRefresh
}