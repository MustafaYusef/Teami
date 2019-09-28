package com.croczi.teami.activities

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
import android.support.v7.app.AppCompatDelegate
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.croczi.teami.R
import com.croczi.teami.models.*
import com.croczi.teami.retrofit.RepresentativesInterface
import kotlinx.android.synthetic.main.activity_add_doctor.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.widget.TextView
import android.view.ViewGroup
import com.croczi.teami.retrofit.NetworkTools
import com.croczi.teami.retrofit.addTLSSupport
import com.croczi.teami.utils.*
import com.croczi.teami.utils.Consts.BASE_URL


class AddDoctor : AppCompatActivity() {

    private var token: String? = null
    private var tokenExp: Long? = 0
    private lateinit var specialtiesList: List<Resource>
    private lateinit var organizationsList: List<Resource>
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
    private var loginResponse: LoginResponse? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_doctor)
        checkUser(this) { status, loginResponse ->
            this.loginResponse = loginResponse

            if (loginResponse != null) {
                token = loginResponse?.token
                tokenExp = loginResponse?.expire
            }
            init()
        }
    }

    private fun init() {
        locationUtils = LocationUtils.getInstance(this)
        finishAddBtn.setOnClickListener {
            if (setValidation() && loginResponse != null)
                addDoctor()
        }
        getSpecialty()
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
                Toast.makeText(this@AddDoctor, getString(R.string.name_empty), Toast.LENGTH_LONG)
                    .show()
                return false
            }
            selectedSpeciality < 1 -> {
                Toast.makeText(
                    this@AddDoctor,
                    getString(R.string.speciality_empty),
                    Toast.LENGTH_LONG
                ).show()
                return false
            }
            selectedOrg < 1 -> {
                Toast.makeText(this@AddDoctor, getString(R.string.org_empty), Toast.LENGTH_LONG)
                    .show()
                return false
            }
            selectedRegion < 1 -> {
                Toast.makeText(this@AddDoctor, getString(R.string.region_empty), Toast.LENGTH_LONG)
                    .show()
                return false
            }
            docBlockET.text.isNullOrBlank() && docBlockET.text.isEmpty() -> {
                Toast.makeText(this@AddDoctor, getString(R.string.block_empty), Toast.LENGTH_LONG)
                    .show()
                return false
            }
            selectedHospital < 1 -> {
                Toast.makeText(
                    this@AddDoctor,
                    getString(R.string.hospital_empty),
                    Toast.LENGTH_LONG
                ).show()
                return false
            }
            selectedWork < 1 -> {
                Toast.makeText(this@AddDoctor, getString(R.string.work_empty), Toast.LENGTH_LONG)
                    .show()
                return false
            }
            else -> return true
        }
    }

    private fun addDoctor() {
        val location = locationUtils.userLocation
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
            NetworkTools.addDoctor(doctor, {
                addDocPB.visibility = View.GONE
                finishAddBtn.visibility = View.VISIBLE
                showMessageOK(getString(R.string.doctor_added_successfully),
                    DialogInterface.OnClickListener { dialog, which ->
                        dialog?.dismiss()
                        docNameET.text.clear()
                    })
            }, { message ->
                addDocPB.visibility = View.GONE
                finishAddBtn.visibility = View.VISIBLE
                Toast.makeText(this@AddDoctor, message, Toast.LENGTH_LONG).show()
            })

        } else {
            Toast.makeText(this@AddDoctor, "Location Unavailable", Toast.LENGTH_LONG).show()
            return
        }
    }

    private fun getSpecialty() {
        token?.let {
            NetworkTools.getSpecialties(it, getID(), {
                specialtiesList = it.specialities
                setSpecialtySpinner()
            }, { message ->
                Toast.makeText(this@AddDoctor, message, Toast.LENGTH_LONG).show()
            })
        } ?: run { checkUser(this) { status, response -> } }
    }

    private fun getOrganizations() {
        token?.let {
            NetworkTools.getOrganizations(it, getID(),
                success = {
                    organizationsList = it.organization
                    setOrgsSpinner()
                }, failure = { message ->
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                })
        } ?: run { checkUser(this) { status, response -> } }
    }

    private fun getRegion() {
        token?.let {
            NetworkTools.getRegions(it, getID(), selectedOrg,
                success = { regionResponse ->
                    regionsList = regionResponse.regions
                    setRegionsSpinner()
                }, failure = { message ->
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                })
        } ?: run { checkUser(this) { status, response -> } }
    }

    private fun getHospitals() {
        token?.let {
            NetworkTools.getHospitals(it, getID(), selectedOrg, {
                hospitalsList = it.hospitals
                setHospitalsSpinner()
            }, { message ->
                Toast.makeText(this@AddDoctor, message, Toast.LENGTH_LONG).show()
            })
        } ?: run { checkUser(this) { status, response -> } }
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
            R.layout.support_simple_spinner_dropdown_item, organizationsList
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

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
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
                        else locationUtils.requestUpdates(this, true)
                    }
                    Activity.RESULT_CANCELED -> locationUtils.initGPS(this)
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            10 -> {
                if ((grantResults.isNotEmpty()
                            && grantResults[0] == PackageManager.PERMISSION_GRANTED
                            && grantResults[1] == PackageManager.PERMISSION_GRANTED
                            && grantResults[2] == PackageManager.PERMISSION_GRANTED)
                ) {
                    locationUtils.getLastKnowLocation(this)
                } else {
                    if (permissionCount > 0) {
                        if (!ActivityCompat.shouldShowRequestPermissionRationale(
                                this,
                                Manifest.permission.WRITE_CONTACTS
                            )
                        ) {
                            showMessageOKCancel(this@AddDoctor,
                                getString(R.string.permissionsTitle),
                                getString(R.string.permissionMessage),
                                DialogInterface.OnClickListener { dialog, which ->
                                    locationUtils.requestUpdates(
                                        this,
                                        true
                                    )
                                },
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
        locationUtils.getLastKnowLocation(this)
        super.onRestart()
    }
}
