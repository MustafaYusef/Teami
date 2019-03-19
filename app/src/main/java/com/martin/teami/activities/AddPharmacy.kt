package com.martin.teami.activities

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
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
import com.martin.teami.utils.checkExpirationLimit
import com.orhanobut.hawk.Hawk
import kotlinx.android.synthetic.main.activity_add_doctor.*
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
    private lateinit var specialtiesList: List<Resource>
    private lateinit var organiztionsList: List<Resource>
    private lateinit var regionsList: List<Resource>
    private lateinit var hospitalsList: List<Resource>
    private lateinit var userLocation: Location
    private var selectedSpeciality = -1
    private var selectedHospital = -1
    private var selectedOrg = 0
    private var selectedRegion = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_pharmacy)
        checkUser()
        finishAddPharmBtn.setOnClickListener {
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
                        pharmNameET.background = ColorDrawable(getColor(R.color.background))
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
        addPharmPB.visibility = View.VISIBLE
        finishAddPharmBtn.visibility = View.INVISIBLE
        val name = pharmNameET.text.toString()
        val street = pharmStreetET.text.toString()
        val pharmacy = Pharmacy(
            name, street, selectedOrg.toString(), selectedRegion.toString()
            ,userLocation.latitude.toString(), userLocation.longitude.toString(), token, getID()
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
                        showMessageOK("Pharmacy Added Successfully!",object :DialogInterface.OnClickListener{
                            override fun onClick(dialog: DialogInterface?, which: Int) {
                                dialog?.dismiss()
                                pharmNameET.text.clear()
                            }
                        })
                    }
//                    else {
//                        val converter = retrofit.responseBodyConverter<ErrorResponse>(
//                            ErrorResponse::class.java,
//                            arrayOfNulls<Annotation>(0)
//                        )
//                        val errors = converter.convert(response.errorBody())
//                        Toast.makeText(this@AddPharmacy, errors?.error?.get(0), Toast.LENGTH_SHORT).show()
//                    }
                }
            })
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
        val adapter = ArrayAdapter(
            this,
            R.layout.support_simple_spinner_dropdown_item, regionsList
        )
        pharmRegionET.setAdapter(adapter)
        pharmRegionET.setOnFocusChangeListener { v, hasFocus ->
            v.isEnabled = true
            if (hasFocus)
                pharmRegionET.showDropDown()
        }
        pharmRegionET.setOnClickListener {
            it.isEnabled = true
            pharmRegionET.showDropDown()
        }
        pharmRegionET.setOnItemClickListener { parent, view, position, id ->
            pharmRegionET.isEnabled = false
            val region:Resource=pharmRegionET.adapter.getItem(position) as Resource
            selectedRegion = region.id
            regionRmvIV2.visibility = View.VISIBLE
        }
        regionRmvIV2.setOnClickListener {
            pharmRegionET.isEnabled = true
            pharmRegionET.text.clear()
            it.visibility = View.GONE
        }
    }

    private fun setOrgsSpinner() {
        val adapter = ArrayAdapter(
            this,
            R.layout.support_simple_spinner_dropdown_item, organiztionsList
        )
        pharmOrganiztionET.setAdapter(adapter)
        pharmOrganiztionET.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus)
                pharmOrganiztionET.showDropDown()
        }
        pharmOrganiztionET.setOnClickListener {
            pharmOrganiztionET.showDropDown()
        }
        pharmOrganiztionET.setOnItemClickListener { parent, view, position, id ->
            pharmOrganiztionET.isEnabled = false
            val org:Resource=pharmOrganiztionET.adapter.getItem(position) as Resource
            selectedOrg = org.id
            getRegion()
            orgRmvIV2.visibility = View.VISIBLE
        }
        orgRmvIV2.setOnClickListener {
            pharmOrganiztionET.isEnabled = true
            pharmOrganiztionET.text.clear()
            it.visibility = View.GONE
        }

    }

    fun getID(): String {
        return Settings.Secure.getString(this@AddPharmacy.contentResolver, Settings.Secure.ANDROID_ID)
    }

    private fun showMessageOK(title: String, okListener: DialogInterface.OnClickListener) {
        AlertDialog.Builder(this@AddPharmacy)
            .setTitle(title)
            .setPositiveButton(getString(R.string.okDialog), okListener)
            .create()
            .show()
    }
}
