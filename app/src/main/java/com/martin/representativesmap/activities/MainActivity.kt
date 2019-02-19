package com.martin.representativesmap.activities

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem
import kotlinx.android.synthetic.main.activity_main.*
import android.support.v4.content.ContextCompat
import android.support.annotation.ColorRes
import android.view.Menu
import android.view.MenuItem
import com.martin.representativesmap.fragments.HomeFragment
import com.martin.representativesmap.fragments.MapFragment
import com.martin.representativesmap.R
import com.martin.representativesmap.adapters.BottomBarAdapter
import com.martin.representativesmap.models.LoginResponse
import com.martin.representativesmap.utils.Consts.LOGIN_RESPONSE_SHARED
import com.martin.representativesmap.utils.Consts.LOGIN_TIME
import com.martin.representativesmap.utils.checkExpirationLimit
import com.martin.representativesmap.utils.logoutUser
import com.orhanobut.hawk.Hawk
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var token: String
    private var tokenExp: Int = 0
    private var calendar: Calendar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Hawk.init(this).build()
        val loginResponse = Hawk.get<LoginResponse>(LOGIN_RESPONSE_SHARED)
        calendar = Hawk.get(LOGIN_TIME)
        if (loginResponse != null) {
            token = loginResponse.token
            tokenExp = loginResponse.expire
            checkExpirationLimit(token, tokenExp, calendar, this)
        } else {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
        setBottomNav()

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.logout -> logoutUser(this, token)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setBottomNav() {
        val item1 = AHBottomNavigationItem(
            getString(R.string.bottomnav_title_0),
            R.drawable.ic_home_white_24dp
        )
        val item2 = AHBottomNavigationItem(
            getString(R.string.bottomnav_title_1),
            R.drawable.ic_map_white_24dp
        )
        bottomNavigation.addItem(item1)
        bottomNavigation.addItem(item2)
        bottomNavigation.setUseElevation(true, 8f)
        bottomNavigation.setTitleTextSize(54f, 40f)
        bottomNavigation.titleState = AHBottomNavigation.TitleState.ALWAYS_SHOW
        bottomNavigation.defaultBackgroundColor = fetchColor(R.color.colorPrimary)
        bottomNavigation.accentColor = fetchColor(R.color.background)
        bottomNavigation.inactiveColor = fetchColor(R.color.colorPrimaryDark)
        bottomNavigation.isBehaviorTranslationEnabled = true
        bottomNavigation.setOnTabSelectedListener { position, wasSelected ->
            if (!wasSelected) {
                checkExpirationLimit(token, tokenExp, calendar, this@MainActivity)
                viewPager.currentItem = position
            }
            return@setOnTabSelectedListener true
        }
        val fragmentsList = listOf(
            HomeFragment(),
            MapFragment()
        )
        val pagerAdapter = BottomBarAdapter(supportFragmentManager, fragmentsList)
        viewPager.adapter = pagerAdapter
        viewPager.setPagingEnabled(false)
        bottomNavigation.currentItem = 0
    }

    private fun fetchColor(@ColorRes color: Int): Int {
        return ContextCompat.getColor(this, color)
    }
}
