package com.croczi.teami.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.croczi.teami.R
import com.croczi.teami.adapters.TabAdapter
import com.croczi.teami.fragments.*
import com.croczi.teami.models.LoginResponse
import com.croczi.teami.models.MeRequest
import com.croczi.teami.models.MeResponse
import com.croczi.teami.retrofit.NetworkTools
import com.croczi.teami.retrofit.RepresentativesInterface
import com.croczi.teami.retrofit.addTLSSupport
import com.croczi.teami.utils.Consts.BASE_URL
import com.croczi.teami.utils.Consts.SHOULD_LOGOUT
import com.croczi.teami.utils.UserStatus
import com.croczi.teami.utils.checkUser
import com.orhanobut.hawk.Hawk
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.error_layout.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class ProfileActivity : AppCompatActivity() {
    private lateinit var loginResponse: LoginResponse
    private lateinit var adapter: TabAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
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
        checkUser(this) { status, loginResponse ->
            when (status) {
                UserStatus.LoggedOut -> logout()
                UserStatus.NetworkError -> updateUi(UiStatus.ShowError)
                UserStatus.LoggedIn -> {
                    loginResponse?.let {
                        this.loginResponse = loginResponse
                        getUserData(getID())
                    }
                }
            }
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
        intent.putExtra(SHOULD_LOGOUT, true)
        finish()
        startActivity(intent)
    }

    private fun getUserData(phoneId: String) {
        updateUi(UiStatus.ShowProgress)
        NetworkTools.getUserInfo(MeRequest(loginResponse.token, phoneId), {
            updateUi(UiStatus.ShowContent)
            val meResponse = it
            repNameTV.text = it.user.UserName
            setTabs(meResponse)
        }, {
            updateUi(UiStatus.ShowError)
        })
    }

    private fun setTabs(meResponse: MeResponse?) {
        val infoFragment = InfoFragment()
        val areaFragment = AreasFragment()
        val args = Bundle()
        args.putString("token", loginResponse.token)
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
        tabsViewPager.offscreenPageLimit = 8
        tabBar.setViewPager(tabsViewPager)
        tabBar.typeface = ResourcesCompat.getFont(this, R.font.cairo_semibold)
    }

    @SuppressLint("HardwareIds")
    fun getID(): String {
        return Settings.Secure.getString(
            this@ProfileActivity.contentResolver,
            Settings.Secure.ANDROID_ID
        )
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_about, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.logout -> logout()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        this.finish()
        return true
    }

    private fun updateUi(status: UiStatus) {
        when (status) {
            UiStatus.ShowError -> {
                errorLayout.visibility = View.VISIBLE
                progressBar.visibility = View.GONE
                contentLayout.visibility = View.INVISIBLE
            }
            UiStatus.ShowContent -> {
                errorLayout.visibility = View.INVISIBLE
                progressBar.visibility = View.GONE
                contentLayout.visibility = View.VISIBLE
            }
            UiStatus.ShowProgress -> {
                errorLayout.visibility = View.INVISIBLE
                progressBar.visibility = View.VISIBLE
                contentLayout.visibility = View.INVISIBLE
            }
        }
    }
}

