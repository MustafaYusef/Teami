package com.croczi.teami.fragments


import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatDelegate
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast

import com.croczi.teami.R
import com.croczi.teami.activities.ForgotPasswordActivity
import com.croczi.teami.activities.MainActivity
import com.croczi.teami.models.ErrorResponse
import com.croczi.teami.models.ErrorResponseArray
import com.croczi.teami.models.LoginRequest
import com.croczi.teami.models.LoginResponse
import com.croczi.teami.retrofit.RepresentativesInterface
import com.croczi.teami.retrofit.addTLSSupport
import com.croczi.teami.utils.Consts
import com.croczi.teami.utils.Consts.BASE_URL
import com.croczi.teami.utils.Consts.LOGIN_RESPONSE_SHARED
import com.croczi.teami.utils.Consts.LOGIN_TIME
import com.orhanobut.hawk.Hawk
import com.wajahatkarim3.easyvalidation.core.view_ktx.validEmail
import kotlinx.android.synthetic.main.activity_login.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import java.util.Collections.singletonList
import okhttp3.OkHttpClient
import okhttp3.CipherSuite
import okhttp3.TlsVersion
import okhttp3.ConnectionSpec
import okhttp3.internal.Version


class LoginFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        super.onViewCreated(view, savedInstanceState)
        loginBtn?.setOnClickListener {
            if (setValidation())
                initLogin()
        }
        forgotPasswordTV?.setOnClickListener {
            val intent = Intent(context, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }
        Hawk.init(context).build()
    }

    private fun setValidation(): Boolean {
        return when {
            emailET?.text.isNullOrBlank() && emailET?.text?.isEmpty()?:true -> {
                Toast.makeText(context, getString(R.string.email_empty), Toast.LENGTH_LONG).show()
                false
            }
            !(emailET?.validEmail()?:false) -> {
                Toast.makeText(context, getString(R.string.email_not_valid), Toast.LENGTH_LONG).show()
                false
            }
            passwordET?.text.isNullOrBlank() && passwordET?.text?.isEmpty()?:true -> {
                Toast.makeText(context, getString(R.string.password_empty), Toast.LENGTH_LONG).show()
                false
            }
            else -> true
        }
    }

    private fun initLogin() {
        var retrofitBuilder = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            retrofitBuilder.addTLSSupport()
        }
        val retrofit = retrofitBuilder.build()
        val loginInterface = retrofit.create(RepresentativesInterface::class.java)
        loginProgressBar?.visibility = View.VISIBLE
        loginBtn?.visibility = View.GONE
        val loginRequest = LoginRequest(emailET?.text.toString(), passwordET?.text.toString(), getID())
        val loginResponseCall = loginInterface.getToken(loginRequest)
        loginResponseCall.enqueue(object : Callback<LoginResponse> {
            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                loginProgressBar?.visibility = View.GONE
                loginBtn?.visibility = View.VISIBLE
                Toast.makeText(context, "Problem with the network", Toast.LENGTH_LONG).show()
            }

            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                loginProgressBar?.visibility = View.GONE
                loginBtn?.visibility = View.VISIBLE
                val loginResponse = response.body()
                if (!loginResponse?.token.isNullOrEmpty()) {
                    Hawk.put(LOGIN_RESPONSE_SHARED, loginResponse)
                    Hawk.put(LOGIN_TIME, Calendar.getInstance(TimeZone.getDefault()))
                    (requireActivity() as MainActivity).gotoMain()
                } else if (response.code() == 406) {
                    val converter = retrofit.responseBodyConverter<ErrorResponse>(
                        ErrorResponse::class.java,
                        arrayOfNulls<Annotation>(0)
                    )
                    response.errorBody()?.let { errorBody ->
                        val errors = converter.convert(errorBody)
                        Toast.makeText(requireContext(), errors?.error, Toast.LENGTH_SHORT).show()
                    }
                } else if (response.code() == 400 || response.code() == 422) {
                    val converter = retrofit.responseBodyConverter<ErrorResponseArray>(
                        ErrorResponseArray::class.java,
                        arrayOfNulls<Annotation>(0)
                    )
                    response.errorBody()?.let { errorBody ->
                        val errors = converter.convert(errorBody)
                        Toast.makeText(requireContext(), errors?.error?.get(0), Toast.LENGTH_SHORT).show()
                    }
                }
            }

        })

    }

    @SuppressLint("HardwareIds")
    fun getID(): String {
        return Settings.Secure.getString(context?.contentResolver, Settings.Secure.ANDROID_ID)
    }
}
