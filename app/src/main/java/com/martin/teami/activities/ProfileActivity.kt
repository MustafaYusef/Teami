package com.martin.teami.activities

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import com.martin.teami.R
import com.martin.teami.adapters.AreaAdapter
import com.martin.teami.models.*
import com.martin.teami.retrofit.RepresentativesInterface
import com.martin.teami.utils.Consts
import com.martin.teami.utils.Consts.LOGIN_RESPONSE_SHARED
import com.martin.teami.utils.Consts.LOGIN_TIME
import com.martin.teami.utils.checkExpirationLimit
import com.martin.teami.utils.logoutUser
import com.orhanobut.hawk.Hawk
import kotlinx.android.synthetic.main.activity_profile.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*

class ProfileActivity : AppCompatActivity() {

    private lateinit var token: String
    private var tokenExp: Long = 0
    private var calendar: Calendar? = null
    private var lastPosition = -1
    private val FADE_DURATION: Long = 500

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        checkUser()
        reloadIV.setOnClickListener {
            checkUser()
        }
    }

    private fun checkUser() {
        val loginResponse = Hawk.get<LoginResponse>(LOGIN_RESPONSE_SHARED)
        calendar = Hawk.get(LOGIN_TIME)
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
        errorLayout.visibility = View.INVISIBLE
        progressBar.visibility = View.VISIBLE
        contentLayout.visibility = View.INVISIBLE
        val retrofit = Retrofit.Builder()
            .baseUrl(Consts.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val userCallback = retrofit.create(RepresentativesInterface::class.java)
            .getMe(MeRequest(token, phoneId)).enqueue(object : Callback<MeResponse> {
                override fun onFailure(call: Call<MeResponse>, t: Throwable) {
                    errorTV.text = t.message
                    progressBar.visibility = View.GONE
                    errorLayout.visibility = View.VISIBLE
                    contentLayout.visibility = View.INVISIBLE
                }

                override fun onResponse(call: Call<MeResponse>, response: Response<MeResponse>) {
                    progressBar.visibility = View.GONE
                    contentLayout.visibility = View.VISIBLE
                    val meResponse = response.body()
                    showUserInfo(meResponse?.user)
                    setAnimation(cardView4, 0)
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

    fun setAnimation(viewToAnimate: View, position: Int) {
        if (position > lastPosition) {
            val anim = ScaleAnimation(
                0.8f,
                1.0f,
                0.8f,
                1.0f,
                Animation.RELATIVE_TO_SELF,
                0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f
            )
            anim.duration = FADE_DURATION//to make duration random number between [0,501)
            viewToAnimate.startAnimation(anim)
            lastPosition = position
        }
    }

    fun getID(): String {
        return Settings.Secure.getString(this@ProfileActivity.contentResolver, Settings.Secure.ANDROID_ID)
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

    override fun onSupportNavigateUp(): Boolean {
        this.finish()
        return true
    }
}
