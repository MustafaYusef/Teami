package com.martin.teami.activities

import android.app.Dialog
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.martin.teami.R
import com.martin.teami.models.*
import com.martin.teami.retrofit.RepresentativesInterface
import com.martin.teami.utils.Consts.BASE_URL
import com.martin.teami.utils.Consts.LOGIN_RESPONSE_SHARED
import com.orhanobut.hawk.Hawk
import kotlinx.android.synthetic.main.activity_full_details.*
import kotlinx.android.synthetic.main.feedback_popup.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class FullDetailsActivity : AppCompatActivity() {

    private var itemsOrdered: ArrayList<Item>? = null
    private lateinit var doctor: MyDoctor
    private lateinit var token: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_details)

        val loginResponse = Hawk.get<LoginResponse>(LOGIN_RESPONSE_SHARED)
        if (loginResponse != null) {
            token = loginResponse.token
        }
        doctor = intent.getParcelableExtra("DOCTOR")
        setDoc()

        orderBtn.setOnClickListener {
            val intent = Intent(this, OrderActivity::class.java)
            if (itemsOrdered != null) {
                intent.putParcelableArrayListExtra("ITEMS_ORDERED", itemsOrdered)
            }
            startActivityForResult(intent, 101)
        }

        feedbackBtn.setOnClickListener {
            val dialog = Dialog(this)
            dialog.setContentView(R.layout.feedback_popup)
            dialog.show()
            dialog.feedbackRatingBar.setOnRatingBarChangeListener { ratingBar, rating, fromUser ->
                when (rating) {
                    1f -> dialog.statueTV.text = "bad"
                    2f -> dialog.statueTV.text = "medium"
                    3f -> dialog.statueTV.text = "good"
                    4f -> dialog.statueTV.text = "very good"
                    5f -> dialog.statueTV.text = "excellent"
                    else -> dialog.statueTV.text = ""
                }
            }
            var statusFull = dialog.statueTV.text.toString()
            var noteFull = ""
            var ratingFull = 0f
            if (ratingFull != null && !noteFull.isNullOrEmpty() && noteFull.isNotBlank()) {
                dialog.feedbackNoteET.setText(noteFull)
                dialog.feedbackRatingBar.rating = ratingFull - 1
            }
            dialog.doneFeedbackBtn.setOnClickListener {
                val rating = dialog.feedbackRatingBar.rating
                val note = dialog.feedbackNoteET.text.toString()
                val status = dialog.statueTV.text.toString()
                if (rating != null && !note.isNullOrEmpty() && note.isNotBlank()) {
                    ratingFull = rating + 1
                    noteFull = note
                    statusFull = status
                    postFeedback(ratingFull,noteFull)
                    dialog.dismiss()
                } else Toast.makeText(
                    this@FullDetailsActivity,
                    "Please, fill all the fields!",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun postFeedback(rating: Float,note:String) {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val feedbackRequest = FeedbackRequest(token, "doctors"
            , doctor.id.toString()
            ,rating.toString(),note
            ,"visit")
        val feedbackResponse = retrofit.create(RepresentativesInterface::class.java)
            .postFeedback(feedbackRequest).enqueue(object :Callback<FeedbackResponse>{
                override fun onFailure(call: Call<FeedbackResponse>, t: Throwable) {

                }

                override fun onResponse(call: Call<FeedbackResponse>, response: Response<FeedbackResponse>) {

                }
            })
    }

    private fun setDoc() {
        docNameTV.text = doctor.name
        specialtyTV.text = doctor.speciality.name
        doctorNameTV.text = doctor.name
        doctorAddrTV.text = doctor.reign.name
        doctorStTV.text = doctor.street
        doctorWorkTV.text = when (doctor.workTime) {
            "p" -> "PM"
            "a" -> "AM"
            "b" -> "AM & PM"
            else -> "NaN"
        }
        docHospitalTV.text = doctor.hospital.name
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            101 -> {
                itemsOrdered = data?.getParcelableArrayListExtra<Item>("ITEMS_ORDERED")
            }
            else -> return
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}
