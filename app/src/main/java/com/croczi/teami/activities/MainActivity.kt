package com.croczi.teami.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.croczi.teami.R
import com.croczi.teami.fragments.LoginFragment
import com.croczi.teami.fragments.MainFragment
import com.croczi.teami.models.*
import com.croczi.teami.utils.*
import com.google.firebase.iid.FirebaseInstanceId
import com.orhanobut.hawk.Hawk


class MainActivity : AppCompatActivity() {
    private var token: String? = null
    private var tokenExp: Long? = 0
    private var loginResponse: LoginResponse? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme_NoActionBar)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Hawk.init(this).build()
        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener { instanceIdResult ->
            val newToken = instanceIdResult.token
            Log.d("Token",newToken)
        }
        getLoginResponse()
        if (intent.getBooleanExtra("logout", false))
            gotoMain()
        else
            checkUser(this)
    }

    fun getLoginResponse() {
        loginResponse = checkUser(this)
        if (loginResponse != null) {
            token = loginResponse?.token
            tokenExp = loginResponse?.expire
        } else {
            gotoLogin()
        }
    }

    fun gotoLogin() {
        val loginFragment = LoginFragment()
        supportFragmentManager.fragments.clear()
        supportFragmentManager.beginTransaction().add(R.id.fragLayout, loginFragment).commitAllowingStateLoss()
    }

    fun gotoMain() {
        val args = Bundle()
        args.putParcelable(Consts.LOGIN_RESPONSE_SHARED, loginResponse)
        if (supportFragmentManager.fragments.isEmpty() || supportFragmentManager.fragments.last() !is MainFragment) {
            val mainFragment = MainFragment()
            mainFragment.arguments = args
            supportFragmentManager?.fragments?.clear()
            supportFragmentManager?.beginTransaction()?.add(R.id.fragLayout, mainFragment)?.commitAllowingStateLoss()
        }
    }
}
