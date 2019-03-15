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
import android.widget.TextView
import android.view.ViewGroup


class AddDoctor : AppCompatActivity(){

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
        addDocPB.visibility=View.VISIBLE
        finishAddBtn.visibility=View.INVISIBLE
        val name = addDocNameET.text.toString()
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
                    addDocPB.visibility=View.GONE
                    finishAddBtn.visibility=View.VISIBLE
                    Toast.makeText(this@AddDoctor, t.message, Toast.LENGTH_LONG).show()
                }

                override fun onResponse(call: Call<AddDoctorResponse>, response: Response<AddDoctorResponse>) {
                    addDocPB.visibility=View.GONE
                    finishAddBtn.visibility=View.VISIBLE
                    if (response.body()?.doctor_id != null) {
                        this@AddDoctor.finish()
                    } else {
                        val converter = retrofit.responseBodyConverter<ErrorResponse>(
                            ErrorResponse::class.java,
                            arrayOfNulls<Annotation>(0)
                        )
                        val errors = converter.convert(response.errorBody())
                        Toast.makeText(this@AddDoctor, errors?.error?.get(0), Toast.LENGTH_SHORT).show()
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
        val adapter = ArrayAdapter(
            this,
            R.layout.support_simple_spinner_dropdown_item,specialtiesList
        )
        addSpecialityET.setAdapter(adapter)
        addSpecialityET.setOnFocusChangeListener { v, hasFocus ->
            v.isEnabled=true
            if(hasFocus)
                addSpecialityET.showDropDown()
        }
        addSpecialityET.setOnClickListener {
            it.isEnabled=true
            addSpecialityET.showDropDown()
        }
        addSpecialityET.setOnItemClickListener { parent, view, position, id ->
            addSpecialityET.isEnabled=false
            selectedSpeciality=position+1
            specialtyRmvIV.visibility=View.VISIBLE
        }
        specialtyRmvIV.setOnClickListener {
            addSpecialityET.isEnabled=true
            addSpecialityET.text.clear()
            it.visibility=View.GONE
        }
    }

    private fun setRegionsSpinner() {
        val adapter = ArrayAdapter(
            this,
            R.layout.support_simple_spinner_dropdown_item, regionsList
        )
        addRegionET.setAdapter(adapter)
        addRegionET.setOnFocusChangeListener { v, hasFocus ->
            v.isEnabled=true
            if(hasFocus)
                addRegionET.showDropDown()
        }
        addRegionET.setOnClickListener {
            it.isEnabled=true
            addRegionET.showDropDown()
        }
        addRegionET.setOnItemClickListener { parent, view, position, id ->
            addRegionET.isEnabled=false
            selectedRegion=position+1
            regionRmvIV.visibility=View.VISIBLE
        }
        regionRmvIV.setOnClickListener {
            addRegionET.isEnabled=true
            addRegionET.text.clear()
            it.visibility=View.GONE
        }
    }

    private fun setOrgsSpinner() {
        val adapter = ArrayAdapter(
            this,
            R.layout.support_simple_spinner_dropdown_item, organiztionsList
        )
        addOrganiztionET.setAdapter(adapter)
        addOrganiztionET.setOnFocusChangeListener { v, hasFocus ->
            if(hasFocus)
                addOrganiztionET.showDropDown()
        }
        addOrganiztionET.setOnClickListener {
            addOrganiztionET.showDropDown()
        }
        addOrganiztionET.setOnItemClickListener { parent, view, position, id ->
            addOrganiztionET.isEnabled=false
            selectedOrg=position+1
            getRegion()
            orgRmvIV.visibility=View.VISIBLE
        }
        orgRmvIV.setOnClickListener {
            addOrganiztionET.isEnabled=true
            addOrganiztionET.text.clear()
            it.visibility=View.GONE
        }
    }

    private fun setHospitalsSpinner() {
        val adapter = ArrayAdapter(
            this,
            R.layout.support_simple_spinner_dropdown_item, hospitalsList
        )
        addHospitalET.setAdapter(adapter)
        addHospitalET.setOnFocusChangeListener { v, hasFocus ->
            v.isEnabled=true
            if(hasFocus)
                addHospitalET.showDropDown()
        }
        addHospitalET.setOnClickListener {
            it.isEnabled=true
            addHospitalET.showDropDown()
        }
        addHospitalET.setOnItemClickListener { parent, view, position, id ->
            addHospitalET.isEnabled=false
            selectedHospital=position+1
            hospitalRmvIV.visibility=View.VISIBLE
        }
        hospitalRmvIV.setOnClickListener {
            addHospitalET.isEnabled=true
            addHospitalET.text.clear()
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
        workSpinner.adapter = adapter
        workSpinner.onItemSelectedListener=object :AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedWork=position
            }
        }
    }

    fun getID(): String {
        return Settings.Secure.getString(this@AddDoctor.contentResolver, Settings.Secure.ANDROID_ID)
    }

    fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        when (parent?.id) {
            R.id.addSpecialityET -> {
                selectedSpeciality = position + 1
                return
            }
            R.id.addHospitalET -> {
                selectedHospital = position
                return
            }
            R.id.addOrganiztionET -> {
                getRegion()
                selectedOrg = position
                return
            }
            R.id.addRegionET -> {
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
