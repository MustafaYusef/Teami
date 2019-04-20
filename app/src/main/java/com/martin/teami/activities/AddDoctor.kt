package com.martin.teami.activities

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.PermissionChecker
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.martin.teami.R
import com.martin.teami.models.*
import com.martin.teami.retrofit.RepresentativesInterface
import com.martin.teami.utils.Consts.BASE_URL
import com.orhanobut.hawk.Hawk
import kotlinx.android.synthetic.main.activity_add_doctor.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import android.widget.TextView
import android.view.ViewGroup
import com.martin.teami.utils.*


class AddDoctor : AppCompatActivity() {

    private var token: String?=null
    private var tokenExp: Long? = 0
    private var calendar: Calendar? = null
    private lateinit var specialtiesList: List<Resource>
    private lateinit var organiztionsList: List<Resource>
    private lateinit var regionsList: List<Resource>
    private lateinit var hospitalsList: List<Resource>
    private lateinit var userLocation: Location
    private var selectedSpeciality = -1
    private var selectedHospital = -1
    private var selectedOrg = -1
    private var selectedRegion = -1
    private var selectedWork = -1
    private lateinit var locationUtils: LocationUtils
    private var permissionCount = 0
    private var loginResponse: LoginResponse?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_doctor)
        loginResponse = checkUser(this)
        if (loginResponse != null) {
            token = loginResponse?.token
            tokenExp = loginResponse?.expire
        }
        locationUtils = LocationUtils(this@AddDoctor)
        locationUtils.initLocation()
        finishAddBtn.setOnClickListener {
            loginResponse = checkUser(this)
            if (setValidation() && loginResponse != null)
                addDoctor()
        }
        getSpeciatly()
        getRegion()
        getOrganizations()
        getHospitals()
        setWorkSpinner()
        docNameET.afterTextChanged {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!it.isEmpty()) {

                    docNameET.background = ColorDrawable(getColor(R.color.colorPrimary))
                    docNameET.setTextColor(resources.getColor(R.color.background))
                } else {
                    docNameET.background = getDrawable(R.drawable.edittext_normal)
                    docNameET.setTextColor(Color.parseColor("#666666"))
                }
            }
        }
    }

    private fun setValidation(): Boolean {
        when {
            docNameET.text.isNullOrBlank() && docNameET.text.isEmpty() -> {
                Toast.makeText(this@AddDoctor, getString(R.string.name_empty), Toast.LENGTH_LONG).show()
                return false
            }
            selectedSpeciality < 1 -> {
                Toast.makeText(this@AddDoctor, getString(R.string.speciality_empty), Toast.LENGTH_LONG).show()
                return false
            }
            selectedOrg < 1 -> {
                Toast.makeText(this@AddDoctor, getString(R.string.org_empty), Toast.LENGTH_LONG).show()
                return false
            }
            selectedRegion < 1 -> {
                Toast.makeText(this@AddDoctor, getString(R.string.region_empty), Toast.LENGTH_LONG).show()
                return false
            }
            docBlockET.text.isNullOrBlank() && docBlockET.text.isEmpty() -> {
                Toast.makeText(this@AddDoctor, getString(R.string.block_empty), Toast.LENGTH_LONG).show()
                return false
            }
            selectedHospital < 1 -> {
                Toast.makeText(this@AddDoctor, getString(R.string.hospital_empty), Toast.LENGTH_LONG).show()
                return false
            }
            selectedWork < 1 -> {
                Toast.makeText(this@AddDoctor, getString(R.string.work_empty), Toast.LENGTH_LONG).show()
                return false
            }
            else -> return true
        }
    }

    private fun addDoctor() {
        var location = locationUtils.userLocation
        if (location != null) {
            userLocation = location

            addDocPB.visibility = View.VISIBLE
            finishAddBtn.visibility = View.INVISIBLE
            val name = docNameET.text.toString()
            val street = docBlockET.text.toString()
            val work = when (selectedWork) {
                1 -> "a"
                2 -> "p"
                3 -> "b"
                else -> "NaN"
            }
            val doctor = Doctor(
                name,
                street,
                selectedOrg.toString(),
                selectedSpeciality.toString(),
                selectedRegion.toString(),
                selectedHospital.toString(),
                userLocation.latitude.toString(),
                userLocation.longitude.toString(),
                work,
                token,
                getID()
            )
            val retrofit = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(BASE_URL)
                .build()
            val addDoctorResponseCall = retrofit.create(RepresentativesInterface::class.java)
                .addNewDoctor(doctor).enqueue(object : Callback<AddDoctorResponse> {
                    override fun onFailure(call: Call<AddDoctorResponse>, t: Throwable) {
                        addDocPB.visibility = View.GONE
                        finishAddBtn.visibility = View.VISIBLE
                        Toast.makeText(this@AddDoctor, t.message, Toast.LENGTH_LONG).show()
                    }

                    override fun onResponse(call: Call<AddDoctorResponse>, response: Response<AddDoctorResponse>) {
                        addDocPB.visibility = View.GONE
                        finishAddBtn.visibility = View.VISIBLE
                        if (response.body()?.doctor_id != null) {
                            showMessageOK(getString(R.string.doctor_added_successfully),
                                DialogInterface.OnClickListener { dialog, which ->
                                    dialog?.dismiss()
                                    docNameET.text.clear()
                                })
                        } else if (response.code() == 406) {
                            val converter = retrofit.responseBodyConverter<ErrorResponse>(
                                ErrorResponse::class.java,
                                arrayOfNulls<Annotation>(0)
                            )
                            val errors = converter.convert(response.errorBody())
                            Toast.makeText(this@AddDoctor, errors?.error, Toast.LENGTH_SHORT).show()
                        } else if (response.code() == 400) {
                            val converter = retrofit.responseBodyConverter<ErrorResponseArray>(
                                ErrorResponseArray::class.java,
                                arrayOfNulls<Annotation>(0)
                            )
                            val errors = converter.convert(response.errorBody())
                            Toast.makeText(this@AddDoctor, errors?.error?.get(0), Toast.LENGTH_SHORT).show()
                        }
                    }
                })
        } else {
            Toast.makeText(this@AddDoctor, "Location Unavailable", Toast.LENGTH_LONG).show()
            return
        }
    }

    private fun getSpeciatly() {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val specialityResponseCall = token?.let {
            retrofit.create(RepresentativesInterface::class.java)
                .getSpecialty(it, getID()).enqueue(object : Callback<SpecialityResponse> {
                    override fun onFailure(call: Call<SpecialityResponse>, t: Throwable) {
                        Toast.makeText(this@AddDoctor, t.message, Toast.LENGTH_LONG).show()
                    }

                    override fun onResponse(
                        call: Call<SpecialityResponse>,
                        response: Response<SpecialityResponse>
                    ) {
                        val specialityResponse = response.body()
                        specialityResponse?.let {
                            specialtiesList = it.specialities
                            setSpecialtySpinner()
                        }
                    }
                })
        }
    }

    private fun getRegion() {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val regionResponseCall = token?.let {
            retrofit.create(RepresentativesInterface::class.java)
                .getRegion(it, getID(), selectedOrg).enqueue(object : Callback<RegionResponse> {
                    override fun onFailure(call: Call<RegionResponse>, t: Throwable) {
                        Toast.makeText(this@AddDoctor, t.message, Toast.LENGTH_LONG).show()
                    }

                    override fun onResponse(call: Call<RegionResponse>, response: Response<RegionResponse>) {
                        val regionResponse = response.body()
                        regionResponse?.let {
                            regionsList = regionResponse.reigns
                            setRegionsSpinner()
                        }
                    }
                })
        }
    }

    private fun getOrganizations() {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val organizationResponseCall = token?.let {
            retrofit.create(RepresentativesInterface::class.java)
                .getOrgs(it, getID()).enqueue(object : Callback<OrganizationResponse> {
                    override fun onFailure(call: Call<OrganizationResponse>, t: Throwable) {
                        Toast.makeText(this@AddDoctor, t.message, Toast.LENGTH_LONG).show()
                    }

                    override fun onResponse(
                        call: Call<OrganizationResponse>,
                        response: Response<OrganizationResponse>
                    ) {
                        val organizationResponse = response.body()
                        organizationResponse?.let {
                            organiztionsList = it.organization
                            setOrgsSpinner()
                        }
                    }
                })
        }
    }

    private fun getHospitals() {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val hospitalResponseCall = token?.let {
            retrofit.create(RepresentativesInterface::class.java)
                .getHospitals(it, getID(), selectedOrg).enqueue(object : Callback<HospitalsResponse> {
                    override fun onFailure(call: Call<HospitalsResponse>, t: Throwable) {
                        Toast.makeText(this@AddDoctor, t.message, Toast.LENGTH_LONG).show()
                    }

                    override fun onResponse(call: Call<HospitalsResponse>, response: Response<HospitalsResponse>) {
                        val hospitalResponse = response.body()
                        hospitalResponse?.let {
                            hospitalsList = it.hospitals
                            setHospitalsSpinner()
                        }
                    }
                })
        }
    }

    private fun setSpecialtySpinner() {
        docSpecialityET.threshold = 0
        val adapter = ArrayAdapter(
            this,
            R.layout.support_simple_spinner_dropdown_item, specialtiesList
        )
        docSpecialityET.setAdapter(adapter)
        docSpecialityET.setOnFocusChangeListener { v, hasFocus ->
            v.isEnabled = true
            if (hasFocus)
                docSpecialityET.showDropDown()
        }
        docSpecialityET.setOnClickListener {
            it.isEnabled = true
            docSpecialityET.showDropDown()
        }
        docSpecialityET.setOnItemClickListener { parent, view, position, id ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                docSpecialityET.background = ColorDrawable(getColor(R.color.colorPrimary))
                docSpecialityET.setTextColor(resources.getColor(R.color.background))
            }
            val speciality: Resource = docSpecialityET.adapter.getItem(position) as Resource
            selectedSpeciality = speciality.id
            specialtyRmvIV.visibility = View.VISIBLE
        }
        specialtyRmvIV.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                docSpecialityET.background = getDrawable(R.drawable.edittext_normal)
                docSpecialityET.setTextColor(Color.parseColor("#666666"))
            }
            selectedSpeciality = -1
            docSpecialityET.text.clear()
            it.visibility = View.GONE
        }
    }

    private fun setRegionsSpinner() {
        docAreaET.threshold = 0
        val adapter = ArrayAdapter(
            this,
            R.layout.support_simple_spinner_dropdown_item, regionsList
        )
        docAreaET.setAdapter(adapter)
        docAreaET.setOnFocusChangeListener { v, hasFocus ->
            v.isEnabled = true
            if (hasFocus)
                docAreaET.showDropDown()
        }
        docAreaET.setOnClickListener {
            it.isEnabled = true
            docAreaET.showDropDown()
        }
        docAreaET.setOnItemClickListener { parent, view, position, id ->
            docAreaET.isEnabled = false
            val region: Resource = docAreaET.adapter.getItem(position) as Resource
            selectedRegion = region.id
            regionRmvIV.visibility = View.VISIBLE
        }
        regionRmvIV.setOnClickListener {
            docAreaET.isEnabled = true
            docAreaET.text.clear()
            it.visibility = View.GONE
            selectedRegion = -1
        }
    }

    private fun setOrgsSpinner() {
        docProvET.threshold = 0
        val adapter = ArrayAdapter(
            this,
            R.layout.support_simple_spinner_dropdown_item, organiztionsList
        )
        docProvET.setAdapter(adapter)
        docProvET.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus)
                docProvET.showDropDown()
        }
        docProvET.setOnClickListener {
            it.isEnabled = true
            docProvET.showDropDown()
        }
        docProvET.setOnItemClickListener { parent, view, position, id ->
            docProvET.isEnabled = false
            val org: Resource = docProvET.adapter.getItem(position) as Resource
            selectedOrg = org.id
            getRegion()
            getHospitals()
            orgRmvIV.visibility = View.VISIBLE
        }
        orgRmvIV.setOnClickListener {
            docProvET.isEnabled = true
            docProvET.text.clear()
            it.visibility = View.GONE
            selectedOrg = -1
            getRegion()
        }
    }

    private fun setHospitalsSpinner() {
        docHospiET.threshold = 0
        val adapter = ArrayAdapter(
            this,
            R.layout.support_simple_spinner_dropdown_item, hospitalsList
        )
        docHospiET.setAdapter(adapter)
        docHospiET.setOnFocusChangeListener { v, hasFocus ->
            v.isEnabled = true
            if (hasFocus)
                docHospiET.showDropDown()
        }
        docHospiET.setOnClickListener {
            it.isEnabled = true
            docHospiET.showDropDown()
        }
        docHospiET.setOnItemClickListener { parent, view, position, id ->
            docHospiET.isEnabled = false
            val hospital: Resource = docHospiET.adapter.getItem(position) as Resource
            selectedHospital = hospital.id
            hospitalRmvIV.visibility = View.VISIBLE
        }
        hospitalRmvIV.setOnClickListener {
            docHospiET.isEnabled = true
            docHospiET.text.clear()
            it.visibility = View.GONE
            selectedHospital = -1
        }
    }

    private fun setWorkSpinner() {
        val workArray = mutableListOf<CharSequence>()
        workArray.add(getString(R.string.work))
        workArray.add(getString(R.string.am))
        workArray.add(getString(R.string.pm))
        workArray.add(getString(R.string.both))
        val adapter = object : ArrayAdapter<CharSequence>(
            this,
            R.layout.support_simple_spinner_dropdown_item, workArray
        ) {
            override fun isEnabled(position: Int): Boolean {
                return position != 0
            }

            override fun getDropDownView(
                position: Int, convertView: View?,
                parent: ViewGroup
            ): View {
                val mView = super.getDropDownView(position, convertView, parent)
                val mTextView = mView as TextView
                if (position == 0) {
                    mTextView.setTextColor(Color.GRAY)
                } else {
                    mTextView.setTextColor(Color.BLACK)
                }
                return mView
            }
        }
        workSpinner.adapter = adapter
        workSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedWork = position
            }
        }
    }

    fun getID(): String {
        return Settings.Secure.getString(this@AddDoctor.contentResolver, Settings.Secure.ANDROID_ID)
    }

    fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        when (parent?.id) {
            R.id.workSpinner -> {
                selectedWork = position
                return
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            100 -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        if (PermissionChecker.checkSelfPermission(
                                this,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                                this,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            ) != PackageManager.PERMISSION_GRANTED
                        )
                        else locationUtils.requestUpdates()
                    }
                    Activity.RESULT_CANCELED -> locationUtils.initGPS()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            10 -> {
                if ((grantResults.isNotEmpty()
                            && grantResults[0] == PackageManager.PERMISSION_GRANTED
                            && grantResults[1] == PackageManager.PERMISSION_GRANTED
                            && grantResults[2] == PackageManager.PERMISSION_GRANTED)
                ) {
                    locationUtils.getLastKnowLocation()
                } else {
                    if (permissionCount > 0) {
                        if (!ActivityCompat.shouldShowRequestPermissionRationale(
                                this,
                                Manifest.permission.WRITE_CONTACTS
                            )
                        ) {
                            showMessageOKCancel(this@AddDoctor, getString(R.string.permissionsTitle),
                                getString(R.string.permissionMessage),
                                DialogInterface.OnClickListener { dialog, which -> locationUtils.requestUpdates() },
                                DialogInterface.OnClickListener { dialog, which ->
                                    this.finish()
                                })
                            return
                        }
                    } else {
                        permissionCount++
                    }
                }
                return
            }
            else -> return
        }
    }

    private fun showMessageOK(title: String, okListener: DialogInterface.OnClickListener) {
        AlertDialog.Builder(this@AddDoctor)
            .setTitle(title)
            .setPositiveButton(getString(R.string.okDialog), okListener)
            .create()
            .show()
    }


    override fun onStop() {
        locationUtils.stopLocation()
        super.onStop()
    }

    override fun onRestart() {
        locationUtils.getLastKnowLocation()
        super.onRestart()
    }
}
