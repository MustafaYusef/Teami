package com.croczi.teami.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.PermissionChecker
import android.support.v7.app.AppCompatDelegate
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import com.croczi.teami.R
import com.croczi.teami.models.*
import com.croczi.teami.retrofit.NetworkTools
import com.croczi.teami.utils.*
import com.orhanobut.hawk.Hawk
import kotlinx.android.synthetic.main.activity_add_pharmacy.*

class AddPharmacy : AppCompatActivity() {

    private lateinit var token: String
    private var tokenExp: Long? = 0
    private lateinit var loginResponse: LoginResponse
    private lateinit var organizationsList: List<Resource>
    private lateinit var regionsList: List<Resource>
    private lateinit var userLocation: Location
    private var selectedOrg = 0
    private var selectedRegion = -1
    private lateinit var locationUtils: LocationUtils
    private var permissionCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_pharmacy)
        checkUser(this) { status, loginResponse ->
            when (status) {
                UserStatus.LoggedOut -> logout()
                UserStatus.LoggedIn -> {
                    loginResponse?.let {
                        this.loginResponse = it
                        token = it.token
                        tokenExp = it.expire

                       init()
                    }
                }
            }
        }
    }
    private fun init(){
        locationUtils = LocationUtils.getInstance(this)
        finishAddPharmBtn.setOnClickListener {
            if (setValidation())
                addPharm()
        }

        getRegion()
        getOrganizations()
        pharmBlockET.afterTextChanged {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (it.isNotEmpty() && it.isNotBlank()) {
                    pharmNameET.background =
                        ColorDrawable(getColor(R.color.colorPrimary))
                    pharmNameET.setTextColor(resources.getColor(R.color.background))
                } else {
                    pharmNameET.background = getDrawable(R.drawable.edittext_normal)
                    pharmNameET.setTextColor(Color.parseColor("#666666"))
                }
            }
        }
    }

    private fun setValidation(): Boolean {
        when {
            pharmNameET.text.isNullOrBlank() && pharmNameET.text.isEmpty() -> {
                Toast.makeText(this@AddPharmacy, getString(R.string.name_empty), Toast.LENGTH_LONG)
                    .show()
                return false
            }
            selectedOrg < 1 -> {
                Toast.makeText(this@AddPharmacy, getString(R.string.org_empty), Toast.LENGTH_LONG)
                    .show()
                return false
            }
            selectedRegion < 1 -> {
                Toast.makeText(
                    this@AddPharmacy,
                    getString(R.string.region_empty),
                    Toast.LENGTH_LONG
                ).show()
                return false
            }
            pharmBlockET.text.isNullOrBlank() && pharmBlockET.text.isEmpty() -> {
                Toast.makeText(this@AddPharmacy, getString(R.string.block_empty), Toast.LENGTH_LONG)
                    .show()
                return false
            }
            else -> return true
        }
    }

    private fun addPharm() {
        val location = locationUtils.userLocation
        if (location != null) {
            userLocation = location
            addPharmPB.visibility = View.VISIBLE
            finishAddPharmBtn.visibility = View.INVISIBLE
            val name = pharmNameET.text.toString()
            val street = pharmBlockET.text.toString()
            val pharmacy = Pharmacy(
                name,
                street,
                selectedOrg.toString(),
                selectedRegion.toString()
                ,
                userLocation.latitude.toString(),
                userLocation.longitude.toString(),
                token,
                getID()
            )
            NetworkTools.addPharmacy(pharmacy, {
                addPharmPB.visibility = View.GONE
                finishAddPharmBtn.visibility = View.VISIBLE
                showMessageOK(this@AddPharmacy, getString(R.string.pharm_added), "",
                    DialogInterface.OnClickListener { dialog, which ->
                        dialog?.dismiss()
                        pharmNameET.text.clear()
                    })
            }, { message ->
                addPharmPB.visibility = View.GONE
                finishAddPharmBtn.visibility = View.VISIBLE
                Toast.makeText(this@AddPharmacy, message, Toast.LENGTH_LONG).show()
            })
        }
    }

    private fun getOrganizations() {
        NetworkTools.getOrganizations(token, getID(),
            success = {
                organizationsList = it.organization
                setOrgsSpinner()
            }, failure = { message ->
                Toast.makeText(this@AddPharmacy, message, Toast.LENGTH_LONG).show()
            })
    }

    private fun getRegion() {
        NetworkTools.getRegions(token, getID(), selectedOrg,
            success = { regionResponse ->
                regionsList = regionResponse.regions
                setRegionsSpinner()
            }, failure = { message ->
                Toast.makeText(this@AddPharmacy, message, Toast.LENGTH_LONG).show()
            })
    }

    private fun setRegionsSpinner() {
        pharmAreaET.threshold = 0
        val adapter = ArrayAdapter(
            this,
            R.layout.support_simple_spinner_dropdown_item, regionsList
        )
        pharmAreaET.setAdapter(adapter)
        pharmAreaET.setOnFocusChangeListener { v, hasFocus ->
            v.isEnabled = true
            if (hasFocus)
                pharmAreaET.showDropDown()
        }
        pharmAreaET.setOnClickListener {
            it.isEnabled = true
            pharmAreaET.showDropDown()
        }
        pharmAreaET.setOnItemClickListener { parent, view, position, id ->
            pharmAreaET.isEnabled = false
            val region: Resource = pharmAreaET.adapter.getItem(position) as Resource
            selectedRegion = region.id
            regionRmvIV2.visibility = View.VISIBLE
        }
        regionRmvIV2.setOnClickListener {
            selectedRegion = -1
            pharmAreaET.isEnabled = true
            pharmAreaET.text.clear()
            it.visibility = View.GONE
        }
    }

    private fun setOrgsSpinner() {
        pharmProvinceET.threshold = 0
        val adapter = ArrayAdapter(
            this,
            R.layout.support_simple_spinner_dropdown_item, organizationsList
        )
        pharmProvinceET.setAdapter(adapter)
        pharmProvinceET.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus)
                pharmProvinceET.showDropDown()
        }
        pharmProvinceET.setOnClickListener {
            pharmProvinceET.showDropDown()
        }
        pharmProvinceET.setOnItemClickListener { parent, view, position, id ->
            pharmProvinceET.isEnabled = false
            val org: Resource = pharmProvinceET.adapter.getItem(position) as Resource
            selectedOrg = org.id
            getRegion()
            orgRmvIV2.visibility = View.VISIBLE
        }
        orgRmvIV2.setOnClickListener {
            selectedOrg = -1
            pharmProvinceET.isEnabled = true
            pharmProvinceET.text.clear()
            it.visibility = View.GONE
        }

    }

    @SuppressLint("HardwareIds")
    fun getID(): String {
        return Settings.Secure.getString(
            this@AddPharmacy.contentResolver,
            Settings.Secure.ANDROID_ID
        )
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
                            showMessageOKCancel(this@AddPharmacy,
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

    private fun logout() {
        val intent = Intent(this, MainActivity::class.java)
        Hawk.deleteAll()
        intent.flags = Intent
            .FLAG_ACTIVITY_CLEAR_TOP or Intent
            .FLAG_ACTIVITY_NO_HISTORY or Intent
            .FLAG_ACTIVITY_NEW_TASK or Intent
            .FLAG_ACTIVITY_CLEAR_TASK
        intent.putExtra(Consts.SHOULD_LOGOUT, true)
        finish()
        startActivity(intent)
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