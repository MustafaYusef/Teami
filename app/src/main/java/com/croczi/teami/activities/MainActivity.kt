package com.croczi.teami.activities

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.multidex.MultiDex
import android.support.v7.app.AppCompatDelegate
import android.util.Log
import com.croczi.teami.R
import com.croczi.teami.fragments.LoginFragment
import com.croczi.teami.fragments.MainFragment
import com.croczi.teami.models.*
import com.croczi.teami.retrofit.NetworkTools
import com.croczi.teami.retrofit.RepresentativesInterface
import com.croczi.teami.utils.*
import com.croczi.teami.utils.Consts.AppLockedExtra
import com.croczi.teami.utils.Consts.LOCK_URL
import com.croczi.teami.utils.Consts.LOGIN_RESPONSE_SHARED
import com.croczi.teami.utils.Consts.SHOULD_LOGOUT
import com.croczi.teami.utils.UserStatus.*
import com.facebook.drawee.backends.pipeline.Fresco
import com.google.firebase.iid.FirebaseInstanceId
import com.orhanobut.hawk.Hawk
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class MainActivity : AppCompatActivity() {

    private var loginResponse: LoginResponse? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        setTheme(R.style.AppTheme_NoActionBar)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        MultiDex.install(this)
        Fresco.initialize(this)
        Hawk.init(this).build()
        NetworkTools.checkIfAppIsLocked({ appLockedResponse ->
            if (appLockedResponse.isLocked)
                lockApp(appLockedResponse)
            else
                launchApp()
        }, {
            launchApp()
        })
    }

    private fun lockApp(appLockedResponse: AppLockedResponse) {
        val intent = Intent(this, AppLockedActivity::class.java)
        intent.putExtra(AppLockedExtra, appLockedResponse)
        startActivity(intent)
    }

    private fun launchApp() {
//        Hawk.init(this).build()
        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener { instanceIdResult ->
            val newToken = instanceIdResult.token
            Log.d("Token", newToken)
        }
        if (intent.getBooleanExtra(SHOULD_LOGOUT, false))
            gotoLogin()
        else
            checkUser(this) { status, loginResponse ->
                when (status) {
                    LoggedOut -> gotoLogin()
                    NetworkError -> gotoMain() //TODO: Make an actual network error page/fragment
                    LoggedIn -> {
                        this.loginResponse = loginResponse
                        gotoMain()
                    }
                }
            }
    }

    private fun gotoLogin() {
        Hawk.deleteAll()
        val loginFragment = LoginFragment()
        supportFragmentManager.fragments.clear()
        supportFragmentManager.beginTransaction().add(R.id.fragLayout, loginFragment)
            .commitAllowingStateLoss()
    }

    fun gotoMain() {
        val args = Bundle()
        args.putParcelable(LOGIN_RESPONSE_SHARED, Hawk.get(LOGIN_RESPONSE_SHARED))
        if (supportFragmentManager.fragments.isEmpty() || supportFragmentManager.fragments.last() !is MainFragment) {
            val mainFragment = MainFragment()
            mainFragment.arguments = args
            supportFragmentManager?.fragments?.clear()
            supportFragmentManager?.beginTransaction()?.add(R.id.fragLayout, mainFragment)
                ?.commitAllowingStateLoss()
        }
    }
}
