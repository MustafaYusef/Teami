package com.martin.teami.activities

import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Toast
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.martin.teami.R
import com.martin.teami.adapters.AreaAdapter
import com.martin.teami.models.*
import com.martin.teami.retrofit.RepresentativesInterface
import com.martin.teami.utils.Consts
import com.martin.teami.utils.checkExpirationLimit
import com.martin.teami.utils.logoutUser
import com.orhanobut.hawk.Hawk
import kotlinx.android.synthetic.main.activity_about.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import kotlin.collections.ArrayList

class AboutActivity : AppCompatActivity() {

    private lateinit var token: String
    private var tokenExp: Long = 0
    private var calendar: Calendar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        checkUser()
    }

    private fun checkUser() {
        val loginResponse = Hawk.get<LoginResponse>(Consts.LOGIN_RESPONSE_SHARED)
        calendar = Hawk.get(Consts.LOGIN_TIME)
        if (loginResponse != null) {
            token = loginResponse.token
            tokenExp = loginResponse.expire
            getUserData(token, getID())
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

    private fun getUserData(token: String, phoneId: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl(Consts.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val userCallback = retrofit.create(RepresentativesInterface::class.java)
            .getMe(MeRequest(token, phoneId)).enqueue(object : Callback<MeResponse> {
                override fun onFailure(call: Call<MeResponse>, t: Throwable) {
                    Toast.makeText(this@AboutActivity, t.message, Toast.LENGTH_LONG).show()
                }

                override fun onResponse(call: Call<MeResponse>, response: Response<MeResponse>) {
                    val meResponse = response.body()
                    showUserInfo(meResponse?.user)
                }
            })
    }

    private fun showUserInfo(user: User?) {
        user?.let {
            repNameTV.text = user.UserName
            phone.text = user.Phone
            email.text = user.Email
            type.text = user.Role
            sup.text = user.Reporting_to
            val adapter = AreaAdapter(user.Coverage_Area)
            areaRV.layoutManager = LinearLayoutManager(this)
            areaRV.adapter = adapter
        }
    }

    fun getID(): String {
        return Settings.Secure.getString(this@AboutActivity.contentResolver, Settings.Secure.ANDROID_ID)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_about, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.logout -> logoutUser(this, token, getID())
        }
        return super.onOptionsItemSelected(item)
    }
}