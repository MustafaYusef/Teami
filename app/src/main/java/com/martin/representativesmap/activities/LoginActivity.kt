package com.martin.representativesmap.activities

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.MotionEvent
import android.widget.Button
import android.widget.Toast
import com.martin.representativesmap.R
import com.martin.representativesmap.models.LoginRequest
import com.martin.representativesmap.models.LoginResponse
import com.martin.representativesmap.retrofit.RepresentativesInterface
import com.martin.representativesmap.utils.Consts.BASE_URL
import com.martin.representativesmap.utils.Consts.LOGIN_RESPONSE_SHARED
import com.martin.representativesmap.utils.Consts.LOGIN_TIME
import com.orhanobut.hawk.Hawk
import kotlinx.android.synthetic.main.activity_login.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*


class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.martin.representativesmap.R.layout.activity_login)
        initLogin()

    }

    private fun initLogin() {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val loginInterface = retrofit.create(RepresentativesInterface::class.java)
        loginBtn.setOnClickListener {
            val loginRequest = LoginRequest(emailET.text.toString(), passwordET.text.toString())
            val loginResponseCall = loginInterface.getToken(loginRequest)
            loginResponseCall.enqueue(object : Callback<LoginResponse> {
                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    Toast.makeText(this@LoginActivity, t.message, Toast.LENGTH_LONG).show()
                }

                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    val loginResponse = response.body()
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    Hawk.put(LOGIN_RESPONSE_SHARED, loginResponse)
                    Hawk.put(LOGIN_TIME,Calendar.getInstance(TimeZone.getDefault()))
                    if (!loginResponse?.token.isNullOrEmpty()) {
                        startActivity(intent)
                    }
                }

            })
        }
    }

    fun getID(): String {
        return Settings.Secure.getString(this@LoginActivity.contentResolver, Settings.Secure.ANDROID_ID)
    }
}
