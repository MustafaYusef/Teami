package com.martin.teami.activities

import android.content.Intent
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.provider.Settings
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.martin.teami.R
import com.martin.teami.models.*
import com.martin.teami.retrofit.RepresentativesInterface
import com.martin.teami.utils.Consts
import com.martin.teami.utils.Consts.BASE_URL
import com.martin.teami.utils.Consts.USER_LOCATION
import com.martin.teami.utils.checkExpirationLimit
import com.orhanobut.hawk.Hawk
import kotlinx.android.synthetic.main.activity_add_doctor.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import kotlin.collections.ArrayList
import android.widget.TextView
import android.view.ViewGroup


class AddDoctor : AppCompatActivity(), AdapterView.OnItemSelectedListener {

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
    private var selectedOrg = 1
    private var selectedRegion = -1
    private var selectedWork = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_doctor)
        checkUser()
        finishAddBtn.setOnClickListener {
            addDoctor()
        }
        getSpeciatly()
        getRegion()
        getOrganizations()
        getHospitals()
        setWorkSpinner()
    }

    private fun checkUser() {
        val loginResponse = Hawk.get<LoginResponse>(Consts.LOGIN_RESPONSE_SHARED)
        calendar = Hawk.get(Consts.LOGIN_TIME)
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

    private fun addDoctor() {
        val name = addDocNameTV.text.toString()
        val street = addStreetET.text.toString()
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
                    Toast.makeText(this@AddDoctor, t.message, Toast.LENGTH_LONG).show()
                }

                override fun onResponse(call: Call<AddDoctorResponse>, response: Response<AddDoctorResponse>) {
                    if (response.body()?.doctor_id != null) {
                        this@AddDoctor.finish()
                    }
                }
            })
    }

    private fun getSpeciatly() {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val specialityResponseCall = retrofit.create(RepresentativesInterface::class.java)
            .getSpecialty(token, getID()).enqueue(object : Callback<SpecialityResponse> {
                override fun onFailure(call: Call<SpecialityResponse>, t: Throwable) {
                    Toast.makeText(this@AddDoctor, t.message, Toast.LENGTH_LONG).show()
                }

                override fun onResponse(call: Call<SpecialityResponse>, response: Response<SpecialityResponse>) {
                    val specialityResponse = response.body()
                    specialityResponse?.let {
                        specialtiesList = it.specialities
                        setSpecialtySpinner()
                    }
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

    private fun getOrganizations() {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val organizationResponseCall = retrofit.create(RepresentativesInterface::class.java)
            .getOrgs(token, getID()).enqueue(object : Callback<OrganizationResponse> {
                override fun onFailure(call: Call<OrganizationResponse>, t: Throwable) {
                    Toast.makeText(this@AddDoctor, t.message, Toast.LENGTH_LONG).show()
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

    private fun getHospitals() {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val hospitalResponseCall = retrofit.create(RepresentativesInterface::class.java)
            .getHospitals(token, getID()).enqueue(object : Callback<HospitalsResponse> {
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

    private fun setSpecialtySpinner() {
        val list = mutableListOf<CharSequence>()
        list.add(getString(R.string.specialty))
        specialtiesList.forEach {
            list.add(it.text)
        }
        val adapter = object : ArrayAdapter<CharSequence>(
            this,
            R.layout.support_simple_spinner_dropdown_item, list
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
        addSpecialitySpinner.adapter = adapter
        addSpecialitySpinner.onItemSelectedListener = this
    }

    private fun setRegionsSpinner() {
        val list = mutableListOf<CharSequence>()
        list.add(getString(R.string.region))
        regionsList.forEach {
            list.add(it.text)
        }
        val adapter = object : ArrayAdapter<CharSequence>(
            this,
            R.layout.support_simple_spinner_dropdown_item, list
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
        addRegionSpinner.adapter = adapter
        addRegionSpinner.onItemSelectedListener = this
    }

    private fun setOrgsSpinner() {
        val list = mutableListOf<CharSequence>()
        list.add(getString(R.string.organization))
        organiztionsList.forEach {
            list.add(it.text)
        }
        val adapter = object : ArrayAdapter<CharSequence>(
            this,
            R.layout.support_simple_spinner_dropdown_item, list
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
        addOrganiztionSpinner.adapter = adapter
        addOrganiztionSpinner.onItemSelectedListener = this

    }

    private fun setHospitalsSpinner() {
        val list = mutableListOf<CharSequence>()
        list.add(getString(R.string.hospital))
        hospitalsList.forEach {
            list.add(it.text)
        }
        val adapter = object : ArrayAdapter<CharSequence>(
            this,
            R.layout.support_simple_spinner_dropdown_item, list
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
        addHospitalSpinner.adapter = adapter
        addHospitalSpinner.onItemSelectedListener = this
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
        workSpinner.adapter = adapter
        workSpinner.onItemSelectedListener = this
    }

    fun getID(): String {
        return Settings.Secure.getString(this@AddDoctor.contentResolver, Settings.Secure.ANDROID_ID)
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        when (parent?.id) {
            R.id.addSpecialitySpinner -> {
                selectedSpeciality = position + 1
                return
            }
            R.id.addHospitalSpinner -> {
                selectedHospital = position
                return
            }
            R.id.addOrganiztionSpinner -> {
                getRegion()
                selectedOrg = position
                return
            }
            R.id.addRegionSpinner -> {
                selectedRegion = position
                return
            }
            R.id.workSpinner -> {
                selectedWork = position
                return
            }
        }
    }

}
