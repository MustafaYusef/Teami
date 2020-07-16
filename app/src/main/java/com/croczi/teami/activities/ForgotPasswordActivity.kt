package com.croczi.teami.activities

import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import android.view.View
import android.widget.Toast
import com.croczi.teami.R
import com.croczi.teami.models.ForgotRequest
import com.croczi.teami.models.ForgotResponse
import com.croczi.teami.retrofit.RepresentativesInterface
import com.croczi.teami.retrofit.addTLSSupport
import com.croczi.teami.utils.Consts
import com.croczi.teami.utils.Consts.BASE_URL
import kotlinx.android.synthetic.main.activity_forgot_password.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class ForgotPasswordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        forgotBtn.setOnClickListener {
            sendEmail()
        }
    }

    private fun sendEmail() {
        var retrofitBuilder = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
        if(Build.VERSION.SDK_INT<= Build.VERSION_CODES.KITKAT){
            retrofitBuilder.addTLSSupport()
        }
        val retrofit=retrofitBuilder.build()
        val forgotInterface = retrofit.create(RepresentativesInterface::class.java)
        forgotProgressBar.visibility = View.VISIBLE
        forgotBtn.visibility = View.GONE
        val forgotRequest=ForgotRequest(forgotEmailET.text.toString())
        val forgotResponseCall = forgotInterface.sendForgotPasswordEmail(forgotRequest)
            .enqueue(object : Callback<ForgotResponse> {
                override fun onFailure(call: Call<ForgotResponse>, t: Throwable) {
                    forgotProgressBar.visibility = View.GONE
                    forgotBtn.visibility = View.VISIBLE
                    Toast.makeText(this@ForgotPasswordActivity, t.message, Toast.LENGTH_LONG).show()
                }

                override fun onResponse(call: Call<ForgotResponse>, response: Response<ForgotResponse>) {
                    forgotProgressBar.visibility = View.GONE
                    forgotBtn.visibility = View.VISIBLE
                    if (response.body() != null)
                        Toast.makeText(this@ForgotPasswordActivity, response.body()?.status, Toast.LENGTH_LONG).show()
                }
            })
    }
}
