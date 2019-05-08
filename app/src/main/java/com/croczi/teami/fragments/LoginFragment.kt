package com.croczi.teami.fragments


import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast

import com.croczi.teami.R
import com.croczi.teami.activities.ForgotPasswordActivity
import com.croczi.teami.activities.MainActivity
import com.croczi.teami.models.LoginRequest
import com.croczi.teami.models.LoginResponse
import com.croczi.teami.retrofit.RepresentativesInterface
import com.croczi.teami.utils.Consts
import com.orhanobut.hawk.Hawk
import com.wajahatkarim3.easyvalidation.core.view_ktx.validEmail
import kotlinx.android.synthetic.main.activity_login.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*

class LoginFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loginBtn.setOnClickListener {
            if (setValidation())
                initLogin()
        }
        forgotPasswordTV.setOnClickListener {
            val intent = Intent(context, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }
        Hawk.init(context).build()
    }

    private fun setValidation(): Boolean {
        when {
            emailET.text.isNullOrBlank() && emailET.text.isEmpty() -> {
                Toast.makeText(context, getString(R.string.email_empty), Toast.LENGTH_LONG).show()
                return false
            }
            !emailET.validEmail()->{
                Toast.makeText(context, getString(R.string.email_not_valid), Toast.LENGTH_LONG).show()
                return false
            }
            passwordET.text.isNullOrBlank() && passwordET.text.isEmpty() -> {
                Toast.makeText(context, getString(R.string.password_empty), Toast.LENGTH_LONG).show()
                return false
            } else -> return true
        }
    }

    private fun initLogin() {
        val retrofit = Retrofit.Builder()
            .baseUrl(Consts.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val loginInterface = retrofit.create(RepresentativesInterface::class.java)
        loginProgressBar.visibility = View.VISIBLE
        loginBtn.visibility = View.GONE
        val loginRequest = LoginRequest(emailET.text.toString(), passwordET.text.toString(), getID())
        val loginResponseCall = loginInterface.getToken(loginRequest)
        loginResponseCall.enqueue(object : Callback<LoginResponse> {
            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                loginProgressBar.visibility = View.GONE
                loginBtn.visibility = View.VISIBLE
                Toast.makeText(context, t.message, Toast.LENGTH_LONG).show()
            }

            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                loginProgressBar.visibility = View.GONE
                loginBtn.visibility = View.VISIBLE
                val loginResponse = response.body()
                val intent = Intent(context, MainActivity::class.java)
                Hawk.put(Consts.LOGIN_RESPONSE_SHARED, loginResponse)
                Hawk.put(Consts.LOGIN_TIME, Calendar.getInstance(TimeZone.getDefault()))
                if (!loginResponse?.token.isNullOrEmpty()) {
                    (context as MainActivity).getLoginResponse()
                    (context as MainActivity).gotoMain()
                }
//                    else {
//                        val converter = retrofit.responseBodyConverter<ErrorResponse>(
//                            ErrorResponse::class.java,
//                            arrayOfNulls<Annotation>(0)
//                        )
//                        val errors = converter.convert(response.errorBody())
//                        Toast.makeText(this@LoginActivity, errors?.error?.get(0), Toast.LENGTH_SHORT).show()
//                    }
            }

        })

    }

    fun getID(): String {
        return Settings.Secure.getString(context?.contentResolver, Settings.Secure.ANDROID_ID)
    }
}
