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
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.PermissionChecker
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import com.martin.teami.R
import com.martin.teami.models.*
import com.martin.teami.retrofit.RepresentativesInterface
import com.martin.teami.utils.Consts.BASE_URL
import com.martin.teami.utils.Consts.LOGIN_RESPONSE_SHARED
import com.martin.teami.utils.Consts.LOGIN_TIME
import com.martin.teami.utils.Consts.USER_LOCATION
import com.martin.teami.utils.LocationUtils
import com.martin.teami.utils.checkExpirationLimit
import com.martin.teami.utils.showMessageOK
import com.martin.teami.utils.showMessageOKCancel
import com.orhanobut.hawk.Hawk
import kotlinx.android.synthetic.main.activity_add_pharmacy.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*

class AddPharmacy : AppCompatActivity() {
    private lateinit var token: String
    private var tokenExp: Long = 0
    private var calendar: Calendar? = null
    private lateinit var organiztionsList: List<Resource>
    private lateinit var regionsList: List<Resource>
    private lateinit var userLocation: Location
    private var selectedOrg = 0
    private var selectedRegion = -1
    private lateinit var locationUtils: LocationUtils
    private var permissionCount=0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_pharmacy)
        checkUser()
        locationUtils = LocationUtils(this@AddPharmacy)
        locationUtils.initLocation()
        finishAddPharmBtn.setOnClickListener {
            if (setValidation())
                addPharm()
        }

        getRegion()
        getOrganizations()
        pharmNameET.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!s.isNullOrEmpty()) {

                        pharmNameET.background = ColorDrawable(getColor(R.color.colorPrimary))
                        pharmNameET.setTextColor(resources.getColor(R.color.background))
                    } else {
                        pharmNameET.background = getDrawable(R.drawable.edittext_normal)
                        pharmNameET.setTextColor(Color.parseColor("#666666"))
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
    }

    private fun setValidation(): Boolean {
        when {
            pharmNameET.text.isNullOrBlank() && pharmNameET.text.isEmpty() -> {
                Toast.makeText(this@AddPharmacy, getString(R.string.name_empty), Toast.LENGTH_LONG).show()
                return false
            }
            selectedOrg < 1 -> {
                Toast.makeText(this@AddPharmacy, getString(R.string.org_empty), Toast.LENGTH_LONG).show()
                return false
            }
            selectedRegion < 1 -> {
                Toast.makeText(this@AddPharmacy, getString(R.string.region_empty), Toast.LENGTH_LONG).show()
                return false
            }
            pharmBlockET.text.isNullOrBlank() && pharmBlockET.text.isEmpty() -> {
                Toast.makeText(this@AddPharmacy, getString(R.string.block_empty), Toast.LENGTH_LONG).show()
                return false
            }
            else -> return true
        }
    }

    private fun checkUser() {
        val loginResponse = Hawk.get<LoginResponse>(LOGIN_RESPONSE_SHARED)
        calendar = Hawk.get(LOGIN_TIME)
        userLocation = intent.getParcelableExtra(USER_LOCATION)
        if (loginResponse != null) {
            token = loginResponse.token
            tokenExp = loginResponse.expire
            checkExpirationLimit(token, tokenExp, getID(), calendar, this)
        } else {
            val intent = Intent(this, LoginActivity::class.java)
            Hawk.deleteAll()
            intent.flags = Intent
                .FLAG_ACTIVITY_CLEAR_TOP or Intent
                .FLAG_ACTIVITY_NO_HISTORY or Intent
                .FLAG_ACTIVITY_NEW_TASK or Intent
                .FLAG_ACTIVITY_CLEAR_TASK
            finish()
            startActivity(intent)
        }
    }

    private fun addPharm() {
        var location = locationUtils.userLocation
        if (location != null) {
            userLocation = location

            addPharmPB.visibility = View.VISIBLE
            finishAddPharmBtn.visibility = View.INVISIBLE
            val name = pharmNameET.text.toString()
            val street = pharmBlockET.text.toString()
            val pharmacy = Pharmacy(
                name, street, selectedOrg.toString(), selectedRegion.toString()
                , userLocation.latitude.toString(), userLocation.longitude.toString(), token, getID()
            )
            val retrofit = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(BASE_URL)
                .build()
            val addPharmacyResponseCall = retrofit.create(RepresentativesInterface::class.java)
                .addNewPharmacy(pharmacy).enqueue(object : Callback<AddPharmacyResponse> {
                    override fun onFailure(call: Call<AddPharmacyResponse>, t: Throwable) {
                        addPharmPB.visibility = View.GONE
                        finishAddPharmBtn.visibility = View.VISIBLE
                        Toast.makeText(this@AddPharmacy, t.message, Toast.LENGTH_LONG).show()
                    }

                    override fun onResponse(call: Call<AddPharmacyResponse>, response: Response<AddPharmacyResponse>) {
                        addPharmPB.visibility = View.GONE
                        finishAddPharmBtn.visibility = View.VISIBLE
                        if (response.body()?.pharmacy_id != null) {
                            showMessageOK(this@AddPharmacy,getString(R.string.pharm_added),"",
                                DialogInterface.OnClickListener { dialog, which ->
                                    dialog?.dismiss()
                                    pharmNameET.text.clear()
                                })
                        } else if (response.code() == 406) {
                            val converter = retrofit.responseBodyConverter<ErrorResponse>(
                                ErrorResponse::class.java,
                                arrayOfNulls<Annotation>(0)
                            )
                            val errors = converter.convert(response.errorBody())
                            Toast.makeText(this@AddPharmacy, errors?.error, Toast.LENGTH_SHORT).show()
                        } else if (response.code() == 400) {
                            val converter = retrofit.responseBodyConverter<ErrorResponseArray>(
                                ErrorResponseArray::class.java,
                                arrayOfNulls<Annotation>(0)
                            )
                            val errors = converter.convert(response.errorBody())
                            Toast.makeText(this@AddPharmacy, errors?.error?.get(0), Toast.LENGTH_SHORT).show()
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
        val regionResponseCall = retrofit.create(RepresentativesInterface::class.java)
            .getRegion(token, getID(), selectedOrg).enqueue(object : Callback<RegionResponse> {
                override fun onFailure(call: Call<RegionResponse>, t: Throwable) {
                    Toast.makeText(this@AddPharmacy, t.message, Toast.LENGTH_LONG).show()
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

    private fun getOrganizations() {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val organizationResponseCall = retrofit.create(RepresentativesInterface::class.java)
            .getOrgs(token, getID()).enqueue(object : Callback<OrganizationResponse> {
                override fun onFailure(call: Call<OrganizationResponse>, t: Throwable) {
                    Toast.makeText(this@AddPharmacy, t.message, Toast.LENGTH_LONG).show()
                }

                override fun onResponse(call: Call<OrganizationResponse>, response: Response<OrganizationResponse>) {
                    val organizationResponse = response.body()
                    organizationResponse?.let {
                        organiztionsList = it.organization
                        setOrgsSpinner()
                    }
                }
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
            R.layout.support_simple_spinner_dropdown_item, organiztionsList
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

    fun getID(): String {
        return Settings.Secure.getString(this@AddPharmacy.contentResolver, Settings.Secure.ANDROID_ID)
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
                            showMessageOKCancel(this@AddPharmacy,getString(R.string.permissionsTitle),
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

    override fun onStop() {
        locationUtils.stopLocation()
        super.onStop()
    }

    override fun onRestart() {
        locationUtils.getLastKnowLocation()
        super.onRestart()
    }
}