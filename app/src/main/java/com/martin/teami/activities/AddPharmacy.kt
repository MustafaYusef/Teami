package com.martin.teami.activities

import android.content.Intent
import android.graphics.Color
import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import com.martin.teami.R
import com.martin.teami.models.*
import com.martin.teami.retrofit.RepresentativesInterface
import com.martin.teami.utils.Consts
import com.martin.teami.utils.checkExpirationLimit
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
    private lateinit var specialtiesList: List<Resource>
    private lateinit var organiztionsList: List<Resource>
    private lateinit var regionsList: List<Resource>
    private lateinit var hospitalsList: List<Resource>
    private lateinit var userLocation: Location
    private var selectedSpeciality = -1
    private var selectedHospital = -1
    private var selectedOrg = 0
    private var selectedRegion = -1
    private var selectedWork = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_pharmacy)
        checkUser()
        finishAddPharmBtn.setOnClickListener {
            addPharm()
        }
        getRegion()
        getOrganizations()
        setWorkSpinner()
    }

    private fun checkUser() {
        val loginResponse = Hawk.get<LoginResponse>(Consts.LOGIN_RESPONSE_SHARED)
        calendar = Hawk.get(Consts.LOGIN_TIME)
        userLocation = intent.getParcelableExtra(Consts.USER_LOCATION)
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
        addPharmPB.visibility=View.VISIBLE
        finishAddPharmBtn.visibility=View.INVISIBLE
        val name = pharmNameET.text.toString()
        val street = pharmNameET.text.toString()
        val work = when (selectedWork) {
            1 -> "a"
            2 -> "p"
            3 -> "b"
            else -> "a"
        }
        val pharmacy = Pharmacy(
            name, street, selectedOrg.toString(), selectedRegion.toString()
            , userLocation.latitude.toString(), userLocation.longitude.toString(), work, token, getID()
        )
        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(Consts.BASE_URL)
            .build()
        val addPharmacyResponseCall = retrofit.create(RepresentativesInterface::class.java)
            .addNewPharmacy(pharmacy).enqueue(object : Callback<AddPharmacyResponse> {
                override fun onFailure(call: Call<AddPharmacyResponse>, t: Throwable) {
                    addPharmPB.visibility=View.GONE
                    finishAddPharmBtn.visibility=View.VISIBLE
                    Toast.makeText(this@AddPharmacy, t.message, Toast.LENGTH_LONG).show()
                }

                override fun onResponse(call: Call<AddPharmacyResponse>, response: Response<AddPharmacyResponse>) {
                    addPharmPB.visibility=View.GONE
                    finishAddPharmBtn.visibility=View.VISIBLE
                    if (response.body()?.pharmacy_id != null) {
                        this@AddPharmacy.finish()
                    }
                }
            })
    }


    private fun getRegion() {
        val retrofit = Retrofit.Builder()
            .baseUrl(Consts.BASE_URL)
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
            .baseUrl(Consts.BASE_URL)
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
            v.isEnabled=true
            if(hasFocus)
                pharmRegionET.showDropDown()
        }
        pharmRegionET.setOnClickListener {
            it.isEnabled=true
            pharmRegionET.showDropDown()
        }
        pharmRegionET.setOnItemClickListener { parent, view, position, id ->
            pharmRegionET.isEnabled=false
            selectedRegion=position+1
            regionRmvIV2.visibility=View.VISIBLE
        }
        regionRmvIV2.setOnClickListener {
            pharmRegionET.isEnabled=true
            pharmRegionET.text.clear()
            it.visibility=View.GONE
        }
    }

    private fun setOrgsSpinner() {
        val adapter = ArrayAdapter(
            this,
            R.layout.support_simple_spinner_dropdown_item, organiztionsList
        )
        pharmOrganiztionET.setAdapter(adapter)
        pharmOrganiztionET.setOnFocusChangeListener { v, hasFocus ->
            if(hasFocus)
                pharmOrganiztionET.showDropDown()
        }
        pharmOrganiztionET.setOnClickListener {
            pharmOrganiztionET.showDropDown()
        }
        pharmOrganiztionET.setOnItemClickListener { parent, view, position, id ->
            pharmOrganiztionET.isEnabled=false
            selectedOrg=position+1
            getRegion()
            orgRmvIV2.visibility=View.VISIBLE
        }
        orgRmvIV2.setOnClickListener {
            pharmOrganiztionET.isEnabled=true
            pharmOrganiztionET.text.clear()
            it.visibility=View.GONE
        }

    }

    private fun setWorkSpinner() {
        val workArray = mutableListOf<CharSequence>()
        workArray.add("Work")
        workArray.add("AM")
        workArray.add("PM")
        workArray.add("Both")
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
        pharmWorkSpinner.adapter = adapter
        pharmWorkSpinner.onItemSelectedListener=object :AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedWork=position
            }
        }
    }

    fun getID(): String {
        return Settings.Secure.getString(this@AddPharmacy.contentResolver, Settings.Secure.ANDROID_ID)
    }
}
