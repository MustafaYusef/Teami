package com.martin.teami.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.martin.teami.R
import com.martin.teami.models.ForgotRequest
import com.martin.teami.models.ForgotResponse
import com.martin.teami.retrofit.RepresentativesInterface
import com.martin.teami.utils.Consts
import kotlinx.android.synthetic.main.activity_forgot_password.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class ForgotPasswordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        forgotBtn.setOnClickListener {
            sendEmail()
        }
    }

    private fun sendEmail() {
        val retrofit = Retrofit.Builder()
            .baseUrl(Consts.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
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
