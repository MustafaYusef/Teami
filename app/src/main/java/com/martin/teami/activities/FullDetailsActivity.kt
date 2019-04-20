package com.martin.teami.activities

import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.martin.teami.R
import com.martin.teami.models.*
import com.martin.teami.retrofit.RepresentativesInterface
import com.martin.teami.utils.Consts.BASE_URL
import com.martin.teami.utils.Consts.LOGIN_RESPONSE_SHARED
import com.martin.teami.utils.checkUser
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
    private var token: String? = null
    private var tokenExp: Long? = 0
    private var loginResponse: LoginResponse? = null
    private lateinit var fbDialog: Dialog
    private var selectedStatus = -1
    private lateinit var allItems: List<Item>
    private lateinit var selectedReminder: Item
    private lateinit var selectedCall: Item

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_details)

        loginResponse = checkUser(this)
        if (loginResponse != null) {
            token = loginResponse?.token
            tokenExp = loginResponse?.expire
        }
        resource = intent.getParcelableExtra("RESOURCE")

        setResource()

        orderBtn.setOnClickListener {
            val intent = Intent(this, OrderActivity::class.java)
            intent.putExtra("RESOURCE", resource)
            startActivity(intent)
        }
        if(resource.resourceType!="doctors")
            resourceIcon.setImageResource(R.drawable.ic_pharmacy)
        feedbackBtn.setOnClickListener {
            loginResponse = checkUser(this)
            if (loginResponse != null) {
                if (resource.resourceType == "doctors") {
                    getItems(0)
                } else {
                    getItems(1)
                }
            }
        }
    }

    private fun getPharmacyStatus() {
        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BASE_URL)
            .build()
        token?.let {
            retrofit.create(RepresentativesInterface::class.java).getPharmStatus(it, getID())
                .enqueue(object : Callback<PharmStatusResponse> {
                    override fun onFailure(call: Call<PharmStatusResponse>, t: Throwable) {
                        fbBtnProgressBar.visibility=View.GONE
                        feedbackBtn.visibility=View.VISIBLE
                        Toast.makeText(this@FullDetailsActivity, getString(R.string.error_loading), Toast.LENGTH_LONG)
                            .show()
                    }

                    override fun onResponse(call: Call<PharmStatusResponse>, response: Response<PharmStatusResponse>) {
                        initFeedback(response.body()?.PharmacyStatus)
                    }
                })
        }
    }

    private fun getStatus() {
        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BASE_URL)
            .build()
        token?.let {
            retrofit.create(RepresentativesInterface::class.java).getStatus(it, getID())
                .enqueue(object : Callback<StatusResponse> {
                    override fun onFailure(call: Call<StatusResponse>, t: Throwable) {
                        fbBtnProgressBar.visibility=View.GONE
                        feedbackBtn.visibility=View.VISIBLE
                        Toast.makeText(this@FullDetailsActivity, getString(R.string.error_loading), Toast.LENGTH_LONG)
                            .show()
                    }

                    override fun onResponse(call: Call<StatusResponse>, response: Response<StatusResponse>) {
                        initFeedback(response.body()?.status)
                    }
                })
        }
    }

    private fun initFeedback(status: List<StatusResource>?) {
        fbBtnProgressBar.visibility=View.GONE
        feedbackBtn.visibility=View.VISIBLE
        val dialog = Dialog(this)
        fbDialog = dialog
        dialog.setContentView(R.layout.feedback_popup)
        dialog.window.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
        prepareItems()
        val statusList = status as ArrayList<StatusResource>
        val adapter = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, statusList)
        dialog.status.setAdapter(adapter)
        dialog.status.threshold = 0
        dialog.status.setOnFocusChangeListener { v, hasFocus ->
            v.isEnabled = true
            if (hasFocus)
                dialog.status.showDropDown()
        }
        dialog.status.setOnClickListener {
            it.isEnabled = true
            dialog.status.showDropDown()
        }
        dialog.status.setOnItemClickListener { parent, view, position, id ->
            dialog.status.isEnabled = false
            val stat: StatusResource = dialog.status.adapter.getItem(position) as StatusResource
            selectedStatus = stat.id
            dialog.statusRmvBtn.visibility = View.VISIBLE
        }
        dialog.statusRmvBtn.setOnClickListener {
            dialog.status.isEnabled = true
            dialog.status.text.clear()
            it.visibility = View.GONE
            selectedStatus = -1
        }
        var noteFull: String
        dialog.doneFeedbackBtn.setOnClickListener {
            dialog.fbProgressBar.visibility = View.VISIBLE
            dialog.doneFeedbackBtn.visibility = View.INVISIBLE
            val note = dialog.feedbackNoteET.text.toString()
            if (selectedStatus != -1 && !note.isEmpty() && note.isNotBlank()) {
                noteFull = note
                postFeedback(selectedStatus, noteFull)
            } else {
                Toast.makeText(
                    this@FullDetailsActivity,
                    getString(R.string.fill_all_fields),
                    Toast.LENGTH_LONG
                ).show()
                dialog.fbProgressBar.visibility = View.GONE
                dialog.doneFeedbackBtn.visibility = View.VISIBLE
            }
        }
    }

    private fun getItems(resourceType: Int) {
        fbBtnProgressBar.visibility=View.VISIBLE
        feedbackBtn.visibility=View.INVISIBLE
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val itemsInterface = retrofit.create(RepresentativesInterface::class.java)
        val itemsResponse = token?.let { itemsInterface.getItems(it, getID()) }
        itemsResponse?.enqueue(object : Callback<ItemsResponse> {
            override fun onFailure(call: retrofit2.Call<ItemsResponse>, t: Throwable) {
                fbBtnProgressBar.visibility=View.GONE
                feedbackBtn.visibility=View.VISIBLE
                Toast.makeText(this@FullDetailsActivity, t.message, Toast.LENGTH_LONG).show()
            }

            override fun onResponse(call: retrofit2.Call<ItemsResponse>, response: Response<ItemsResponse>) {
                response.body()?.let {
                    allItems = it.items
                    if (resourceType == 0)
                        getStatus()
                    else
                        getPharmacyStatus()
                }
            }
        })
    }

    private fun prepareItems() {
        val arrayAdapter = ArrayAdapter<Item>(this, R.layout.text_view_layout, allItems)
        fbDialog.reminderTV.setAdapter(arrayAdapter)
        fbDialog.callTV.setAdapter(arrayAdapter)
        fbDialog.reminderTV.setOnClickListener {
            fbDialog.reminderTV.showDropDown()
        }
        fbDialog.reminderRmvBtn.setOnClickListener {
            fbDialog.reminderTV.isEnabled = true
            fbDialog.reminderTV.text.clear()
            it.visibility = View.GONE
            selectedStatus = -1
        }
        fbDialog.callRmvBtn.setOnClickListener {
            fbDialog.callTV.isEnabled = true
            fbDialog.callTV.text.clear()
            it.visibility = View.GONE
            selectedStatus = -1
        }
        fbDialog.reminderTV.setOnFocusChangeListener { v, hasFocus ->
            v.isEnabled = true
            if (hasFocus)
                fbDialog.reminderTV.showDropDown()
        }
        fbDialog.callTV.setOnFocusChangeListener { v, hasFocus ->
            v.isEnabled = true
            if (hasFocus)
                fbDialog.callTV.showDropDown()
        }
        fbDialog.callTV.setOnClickListener {
            fbDialog.callTV.showDropDown()
        }
        fbDialog.reminderTV.threshold = 0
        fbDialog.callTV.threshold = 0
        fbDialog.reminderTV.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                selectedReminder = fbDialog.reminderTV.adapter.getItem(position) as Item
                fbDialog.reminderRmvBtn.visibility = View.VISIBLE
            }

        fbDialog.callTV.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                selectedCall = fbDialog.callTV.adapter.getItem(position) as Item
                fbDialog.callRmvBtn.visibility = View.VISIBLE

            }
    }

    private fun postFeedback(statusId: Int, note: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val feedbackRequest = token?.let {
            FeedbackRequest(
                it,
                getID(),
                resource.resourceType
                ,
                resource.id.toString()
                ,
                statusId.toString(),
                note
                ,
                "visit",
                "${selectedReminder.companyName}_${selectedReminder.name}",
                "${selectedCall.companyName}_${selectedCall.name}"
            )
        }
        val feedbackResponse = feedbackRequest?.let {
            retrofit.create(RepresentativesInterface::class.java)
                .postFeedback(it).enqueue(object : Callback<FeedbackResponse> {
                    override fun onFailure(call: Call<FeedbackResponse>, t: Throwable) {

                    }

                    override fun onResponse(call: Call<FeedbackResponse>, response: Response<FeedbackResponse>) {
                        fbDialog.doneFeedbackBtn.visibility = View.VISIBLE
                        fbDialog.fbProgressBar.visibility = View.GONE
                        if (response.body()?.activity?.id != null) {
                            if (fbDialog.isShowing) {
                                fbDialog.fbProgressBar.visibility = View.GONE
                                fbDialog.doneFeedbackBtn.visibility = View.VISIBLE
                                fbDialog.dismiss()
                            }
                            showMessageOK(this@FullDetailsActivity, getString(R.string.feedback_success), ""
                                , DialogInterface.OnClickListener { dialog, which -> dialog?.dismiss() })
                        } else if (response.code() == 406) {
                            val converter = retrofit.responseBodyConverter<ErrorResponse>(
                                ErrorResponse::class.java,
                                arrayOfNulls<Annotation>(0)
                            )
                            val errors = converter.convert(response.errorBody())
                            Toast.makeText(this@FullDetailsActivity, errors?.error, Toast.LENGTH_SHORT).show()
                        } else if (response.code() == 400 || response.code() == 422) {
                            val converter = retrofit.responseBodyConverter<ErrorResponseArray>(
                                ErrorResponseArray::class.java,
                                arrayOfNulls<Annotation>(0)
                            )
                            val errors = converter.convert(response.errorBody())
                            Toast.makeText(this@FullDetailsActivity, errors?.error?.get(0), Toast.LENGTH_SHORT).show()
                        }
                    }
                })
        }
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

    fun getID(): String {
        return Settings.Secure.getString(this@FullDetailsActivity.contentResolver, Settings.Secure.ANDROID_ID)
    }
}
