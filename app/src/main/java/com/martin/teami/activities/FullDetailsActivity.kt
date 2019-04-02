package com.martin.teami.activities

import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.martin.teami.R
import com.martin.teami.models.*
import com.martin.teami.retrofit.RepresentativesInterface
import com.martin.teami.utils.Consts.BASE_URL
import com.martin.teami.utils.Consts.LOGIN_RESPONSE_SHARED
import com.martin.teami.utils.showMessageOK
import com.orhanobut.hawk.Hawk
import kotlinx.android.synthetic.main.activity_full_details.*
import kotlinx.android.synthetic.main.feedback_popup.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class FullDetailsActivity : AppCompatActivity() {

    private lateinit var resource: MyResources
    private lateinit var token: String
    private lateinit var fbDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_details)

        val loginResponse = Hawk.get<LoginResponse>(LOGIN_RESPONSE_SHARED)
        if (loginResponse != null) {
            token = loginResponse.token
        }
        resource = intent.getParcelableExtra("RESOURCE")

        setResource()

        orderBtn.setOnClickListener {
            val intent = Intent(this, OrderActivity::class.java)
            intent.putExtra("RESOURCE", resource)
            startActivity(intent)
        }

        feedbackBtn.setOnClickListener {
            initFeedback()
        }
    }

    private fun initFeedback() {
        val dialog = Dialog(this)
        fbDialog = dialog
        dialog.setContentView(R.layout.feedback_popup)
        dialog.window.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
        dialog.feedbackRatingBar.setOnRatingBarChangeListener { ratingBar, rating, fromUser ->
            when (rating) {
                1f -> dialog.statueTV.text = getString(R.string.bad)
                2f -> dialog.statueTV.text = getString(R.string.medium)
                3f -> dialog.statueTV.text = getString(R.string.good)
                4f -> dialog.statueTV.text = getString(R.string.very_good)
                5f -> dialog.statueTV.text = getString(R.string.excellent)
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
            dialog.fbProgressBar.visibility = View.VISIBLE
            dialog.doneFeedbackBtn.visibility = View.INVISIBLE
            val rating = dialog.feedbackRatingBar.rating
            val note = dialog.feedbackNoteET.text.toString()
            val status = dialog.statueTV.text.toString()
            if (rating != null && !note.isNullOrEmpty() && note.isNotBlank()) {
                ratingFull = rating + 1
                noteFull = note
                statusFull = status
                postFeedback(ratingFull, noteFull)
            } else Toast.makeText(
                this@FullDetailsActivity,
                getString(R.string.fill_all_fields),
                Toast.LENGTH_LONG
            )
        }
    }

    private fun postFeedback(rating: Float, note: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val feedbackRequest = FeedbackRequest(
            token, resource.resourceType
            , resource.id.toString()
            , rating.toString(), note
            , "visit"
        )
        val feedbackResponse = retrofit.create(RepresentativesInterface::class.java)
            .postFeedback(feedbackRequest).enqueue(object : Callback<FeedbackResponse> {
                override fun onFailure(call: Call<FeedbackResponse>, t: Throwable) {

                }

                override fun onResponse(call: Call<FeedbackResponse>, response: Response<FeedbackResponse>) {
                    if (fbDialog.isShowing) {
                        fbDialog.fbProgressBar.visibility = View.GONE
                        fbDialog.doneFeedbackBtn.visibility = View.VISIBLE
                        fbDialog.dismiss()
                    }
                    showMessageOK(this@FullDetailsActivity, getString(R.string.feedback_success), ""
                        , DialogInterface.OnClickListener { dialog, which -> dialog?.dismiss() })
                }
            })
    }

    private fun setResource() {
        docNameTV.text = resource.name
        doctorAddrTV.text = resource.reign
        doctorStTV.text = resource.street
        if (resource.resourceType == "doctors") {
            specialtyTV.text = resource.speciality
            docHospitalTV.text = resource.hospital
            doctorWorkTV.text = when (resource.workTime) {
                "p" -> "PM"
                "a" -> "AM"
                "b" -> "AM & PM"
                else -> "NaN"
            }
        } else {
            docHospitalTV.visibility = View.GONE
            workHoursCV.visibility = View.GONE
            specialtyTV.visibility = View.GONE

        }

    }
}
