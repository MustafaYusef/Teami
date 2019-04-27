package com.martin.teami.activities

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.support.v4.content.res.ResourcesCompat
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import com.martin.teami.R
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
import com.martin.teami.adapters.TabAdapter
import com.martin.teami.fragments.*
import kotlinx.android.synthetic.main.error_layout.*


class ProfileActivity : AppCompatActivity() {

    private lateinit var token: String
    private var tokenExp: Long = 0
    private var calendar: Calendar? = null
    private lateinit var adapter: TabAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        checkUser()

        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
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
        retrofit.create(RepresentativesInterface::class.java)
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
                    repNameTV.text = response.body()?.user?.UserName
                    setTabs(meResponse)
                }
            })
    }

    private fun setTabs(meResponse: MeResponse?) {
        val infoFragment = InfoFragment()
        val areaFragment = AreasFragment()
        val args = Bundle()
        args.putString("token", token)
        args.putParcelable("me", meResponse)
        adapter = TabAdapter(supportFragmentManager)
        areaFragment.arguments = args
        infoFragment.arguments = args
        adapter.addFragment(infoFragment, getString(R.string.info))
        adapter.addFragment(areaFragment, getString(R.string.areas))
        if (meResponse?.user?.Role == "sales_delegate") {
            val userOrdersFragment = UserOrdersFragment()
            userOrdersFragment.arguments = args
            adapter.addFragment(userOrdersFragment, getString(R.string.user_orders))
            tabBar.setTitles(
                adapter.getPageTitle(0),
                adapter.getPageTitle(1),
                adapter.getPageTitle(2)
            )
        } else {
            val historyFragment = HistoryFragment()
            val performanceFragment = PerformanceFragment()
            historyFragment.arguments = args
            performanceFragment.arguments = args
            adapter.addFragment(historyFragment, getString(R.string.history))
            adapter.addFragment(performanceFragment, getString(R.string.performance))
            tabBar.setTitles(
                adapter.getPageTitle(0),
                adapter.getPageTitle(1),
                adapter.getPageTitle(2),
                adapter.getPageTitle(3)
            )
        }
        tabsViewPager.adapter = adapter
        tabsViewPager.offscreenPageLimit = 0
        tabBar.setViewPager(tabsViewPager)
        tabBar.typeface = ResourcesCompat.getFont(this, R.font.cairo_semibold)
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
