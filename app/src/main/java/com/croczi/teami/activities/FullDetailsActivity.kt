package com.croczi.teami.activities

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatDelegate
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.croczi.teami.R
import com.croczi.teami.database.entitis.FeedbackRequestLocal
import com.croczi.teami.database.entitis.ItemLocal
import com.croczi.teami.models.*
import com.croczi.teami.retrofit.NetworkTools
import com.croczi.teami.retrofit.RepresentativesInterface
import com.croczi.teami.retrofit.addTLSSupport
import com.croczi.teami.utils.*
import com.croczi.teami.utils.Consts.BASE_URL
import com.mustafayusef.holidaymaster.utils.corurtins
import com.mustafayusef.sharay.database.databaseApp
import com.orhanobut.hawk.Hawk
import kotlinx.android.synthetic.main.activity_full_details.*
import kotlinx.android.synthetic.main.feedback_popup.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
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
    private var selectedReminder: Item = Item()
    private var selectedCall: Item = Item()
    var db: databaseApp?=null
   lateinit var userLocation: Location

     var allItemsLocal= mutableListOf<Item>()
    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_details)
        resource = intent.getParcelableExtra("RESOURCE")!!
        userLocation=intent.getParcelableExtra("userLocation") as Location
        setResource()
        db= this?.let { databaseApp(it) }

//        this.loginResponse=Hawk.get<LoginResponse>(Consts.LOGIN_RESPONSE_SHARED)
//        token= this.loginResponse!!.token
//        tokenExp =this.loginResponse!!.expire
        orderProgressBar?.visibility=View.VISIBLE
        orderBtn?.visibility=View.INVISIBLE
        feedbackBtn?.visibility=View.INVISIBLE
            checkUser(this) { status
                              , loginResponse ->
                if(status== UserStatus.NetworkError){
                    this.loginResponse=Hawk.get<LoginResponse>(Consts.LOGIN_RESPONSE_SHARED)
                    token= this.loginResponse!!.token
                    tokenExp =this.loginResponse!!.expire
                    orderProgressBar?.visibility=View.GONE
                    orderBtn?.visibility=View.VISIBLE
                    feedbackBtn?.visibility=View.VISIBLE
                    init()

                }else{
                    this.loginResponse = loginResponse
                    if (loginResponse != null) {
                        token = loginResponse.token
                        tokenExp = loginResponse.expire
                        orderProgressBar?.visibility=View.GONE
                        orderBtn?.visibility=View.VISIBLE
                        feedbackBtn?.visibility=View.VISIBLE
                        init()
                    }
                }
            }




//        corurtins.main {
////            if(db!!.items_Dao().getAllItems().isNullOrEmpty()){
////                (requireActivity() as MainActivity). getItems()
////            }
////            if(db!!.statusDoc_Dao().getAllStatusDoc().isNullOrEmpty()){
////                (requireActivity() as MainActivity). getStatusDoctor()
////            }
////            if(db!!.statusPh_Dao().getAllStatusPharnasy().isNullOrEmpty()){
////                (requireActivity() as MainActivity). getStatusPhar()
////            }
//            println("Itemssssssssssssss "+db!!.items_Dao().getAllItems())
//            println("statuse doctorrrrrrrrrrr  "+db!!.statusDoc_Dao().getAllStatusDoc())
//            println("statuse Pharmasyyyyyyyyy  "+db!!.statusPh_Dao().getAllStatusPharnasy())
//
//        }

    }

    private fun init() {
        orderBtn?.setOnClickListener {
            val intent = Intent(this, OrderActivity::class.java)
            intent.putExtra("RESOURCE", resource)

            startActivity(intent)
        }

        feedbackBtn?.setOnClickListener {
            if(checkIfNearMarker(resource)){
                if (loginResponse != null) {
                    if (resource.resourceType == "doctors") {
                        getItems(0)
                    } else {
                        getItems(1)
                    }
                }
            }else{
                Toast.makeText(this, this. getString(R.string.not_near), Toast.LENGTH_LONG)
                    .show()
            }

        }
    }

//    private fun getPharmacyStatus() {
//        token?.let {
//            NetworkTools.getPharmacyStatus(it, getID(), { response ->
//                initFeedback(response.PharmacyStatus)
//            }, { message ->
//                fbBtnProgressBar?.visibility = View.GONE
//                feedbackBtn?.visibility = View.VISIBLE
//                Toast.makeText(this@FullDetailsActivity, message, Toast.LENGTH_LONG)
//                    .show()
//            })
//        } ?: run { checkUser(this) { _, _ -> } }
//    }
private fun getPharmacyStatus() {
    var allStatus= mutableListOf<StatusResource>()
    token?.let {
       CoroutineScope(IO).launch {
           async {
               var status=db?.statusPh_Dao()!!.getAllStatusPharnasy()
               for(i in 0 until status.size){
                   var item=StatusResource(id=status[i].id,
                       text=status[i].text)
                   allStatus.add(item)
               }
           }.await()
       }
        initFeedback(allStatus)
    } ?: run { checkUser(this) { _, _ -> } }
}
    private fun getStatus() {
        var allStatus= mutableListOf<StatusResource>()
        token?.let {
            CoroutineScope(IO).launch {
                async {

                    var status = db?.statusDoc_Dao()!!.getAllStatusDoc()
                    for (i in 0 until status.size) {
                        var item = StatusResource(
                            id = status[i].id,
                            text = status[i].text
                        )
                        allStatus.add(item)
                    }
                }.await()
            }
            initFeedback(allStatus)
        } ?: run { checkUser(this) { _, _ -> } }
    }
//    private fun getStatus() {
//        token?.let {
//            NetworkTools.getStatus(it, getID(), { response ->
//                initFeedback(response.status)
//            }, { message ->
//                fbBtnProgressBar?.visibility = View.GONE
//                feedbackBtn?.visibility = View.VISIBLE
//                Toast.makeText(
//                    this@FullDetailsActivity,
//                    message,
//                    Toast.LENGTH_LONG
//                )
//                    .show()
//            })
//        } ?: run { checkUser(this) { _, _ -> } }
//    }

    private fun initFeedback(status: List<StatusResource>?) {
       fbBtnProgressBar?.visibility = View.GONE
        feedbackBtn?.visibility = View.VISIBLE
        val dialog = Dialog(this)
        fbDialog = dialog
        dialog.setContentView(R.layout.feedback_popup)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
        if (resource.resourceType == "doctors")
            prepareItems()
        else {
            dialog.callTV.visibility = View.GONE
            dialog.reminderTV.visibility = View.GONE
        }
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
        fbBtnProgressBar?.visibility = View.VISIBLE
        feedbackBtn?.visibility = View.INVISIBLE

        token?.let {
           CoroutineScope(IO).launch {
               async {
                   var itemsLocal=db?.items_Dao()!!.getAllItems()
                   for(i in 0 until itemsLocal.size){
                       var item=Item(id=itemsLocal[i].id,
                           name=itemsLocal[i].name,
                           description=itemsLocal[i].description,
                           companyName=itemsLocal[i].companyName)
                       allItemsLocal.add(item)
                   }
                   allItems = allItemsLocal
               }.await()

           }
                if (resourceType == 0)
                    getStatus()
                else
                    getPharmacyStatus()
            } ?: run { checkUser(this) { _, _ -> } }
    }
//    private fun getItems(resourceType: Int) {
//        fbBtnProgressBar?.visibility = View.VISIBLE
//        feedbackBtn?.visibility = View.INVISIBLE
//        var retrofitBuilder = Retrofit.Builder()
//            .baseUrl(BASE_URL)
//            .addConverterFactory(GsonConverterFactory.create())
//        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
//            retrofitBuilder.addTLSSupport()
//        }
//        val retrofit = retrofitBuilder.build()
//
//        val itemsInterface = retrofit.create(RepresentativesInterface::class.java)
//        token?.let {
//            NetworkTools.getItems(it, getID(), { response ->
//                allItems = response.items
//                if (resourceType == 0)
//                    getStatus()
//                else
//                    getPharmacyStatus()
//            }, { message ->
//
//                fbBtnProgressBar?.visibility = View.GONE
//                feedbackBtn?.visibility = View.VISIBLE
//                Toast.makeText(this@FullDetailsActivity, message, Toast.LENGTH_LONG).show()
//            })
//        } ?: run { checkUser(this) { _, _ -> } }
//    }

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
        val feedbackRequest = token?.let {
            if (resource.resourceType == "doctors")
                FeedbackRequest(
                    it,
                    getID(),
                    resource.resourceType,
                    resource.id.toString(),
                    statusId.toString(),
                    note,
                    "visit",
                    "${selectedReminder.companyName}_${selectedReminder.name}",
                    "${selectedCall.companyName}_${selectedCall.name}"
                )
            else
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
                    "visit", null, null
                )
        }
        feedbackRequest?.let {
            NetworkTools.postFeedback(it, {
                fbDialog.doneFeedbackBtn.visibility = View.VISIBLE
                fbDialog.fbProgressBar.visibility = View.GONE
                if (fbDialog.isShowing) {
                    fbDialog.fbProgressBar.visibility = View.GONE
                    fbDialog.doneFeedbackBtn.visibility = View.VISIBLE
                    fbDialog.dismiss()
                }
                showMessageOK(this@FullDetailsActivity,
                getString(R.string.feedback_success),
                ""
                ,
                DialogInterface.OnClickListener { dialog, which -> dialog?.dismiss() })
            }, { message ->
                var Feed= FeedbackRequestLocal( token=token!!,
                 phoneId=feedbackRequest.phoneId,
                 resourceType=feedbackRequest.resourceType,
                 resourceId=feedbackRequest.resourceId,
                 statusId=feedbackRequest.statusId,
                 note=feedbackRequest.note,
                 activityType=feedbackRequest.activityType,
                 remindersProducts=feedbackRequest.remindersProducts,
                 callProducts=feedbackRequest.callProducts)
                 corurtins.main {
                     db!!.feedBack_Dao().insertFeedBack(Feed)
                 }
                fbDialog.doneFeedbackBtn.visibility = View.VISIBLE
                fbDialog.fbProgressBar.visibility = View.GONE
                if (fbDialog.isShowing) {
                    fbDialog.fbProgressBar.visibility = View.GONE
                    fbDialog.doneFeedbackBtn.visibility = View.VISIBLE
                    fbDialog.dismiss()
                }
                showMessageOK(this@FullDetailsActivity,
                    getString(R.string.Feed_successful_local),
                    ""
                    ,
                    DialogInterface.OnClickListener { dialog, which -> dialog?.dismiss() })
//                Toast.makeText(this@FullDetailsActivity, message, Toast.LENGTH_LONG).show()
            })
        }
    }

    private fun setResource() {
        if (resource.resourceType == "pharmacies")
            resourceIcon.setImageResource(R.drawable.ic_pharmacy)
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
            hospitalTV.visibility = View.GONE
            docHospitalTV.visibility = View.GONE
            workHoursCV.visibility = View.GONE
            specialtyTV.visibility = View.GONE

        }
    }

    @SuppressLint("HardwareIds")
    fun getID(): String {
        return Settings.Secure.getString(
            this@FullDetailsActivity.contentResolver,
            Settings.Secure.ANDROID_ID
        )
    }

    fun checkIfNearMarker(resource: MyResources): Boolean {
        val docLocation = Location("Nearest Doctor")
        docLocation.latitude = resource.latitude.toDouble()
        docLocation.longitude = resource.longitude.toDouble()

        userLocation?.let {
            val distance = it.distanceTo(docLocation)
            return distance < 200
        }
        return false
    }
}
