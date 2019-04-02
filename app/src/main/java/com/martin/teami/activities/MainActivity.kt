package com.martin.teami.activities

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.support.v4.content.ContextCompat
import android.support.annotation.ColorRes
import android.support.v4.app.ActivityCompat
import android.support.v4.content.PermissionChecker
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.Toast
import com.getbase.floatingactionbutton.FloatingActionsMenu
import com.martin.teami.R
import com.martin.teami.adapters.ResourcesAdapter
import com.martin.teami.models.*
import com.martin.teami.retrofit.RepresentativesInterface
import com.martin.teami.utils.Consts.BASE_URL
import com.martin.teami.utils.Consts.LOGIN_RESPONSE_SHARED
import com.martin.teami.utils.Consts.LOGIN_TIME
import com.martin.teami.utils.Consts.USER_LOCATION
import com.martin.teami.utils.LocationUtils
import com.martin.teami.utils.checkExpirationLimit
import com.orhanobut.hawk.Hawk
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*


class MainActivity : AppCompatActivity() {

    private var resourcesList: List<MyResources>? = null
    private var userLocation: Location? = null
    private lateinit var token: String
    private var tokenExp: Long = 0
    private var calendar: Calendar? = null
    private lateinit var adapter: ResourcesAdapter
    private lateinit var locationUtils: LocationUtils
    private var permissionCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Hawk.init(this).build()
        checkUser()
        locationUtils = LocationUtils(this@MainActivity)
        locationUtils.initLocation()
        userLocation=locationUtils.userLocation
        pink_icon.setOnFloatingActionsMenuUpdateListener(object :
            FloatingActionsMenu.OnFloatingActionsMenuUpdateListener {
            override fun onMenuExpanded() {
                dimView.visibility = View.VISIBLE
                dimView.setOnClickListener {
                    pink_icon.collapse()
                }
            }

            override fun onMenuCollapsed() {
                dimView.visibility = View.GONE
            }
        })
        addDocFab.setOnClickListener {
            userLocation=locationUtils.userLocation
            if (userLocation != null) {
                checkUser()
                val intent = Intent(this, AddDoctor::class.java)
                startActivity(intent)
            } else Toast.makeText(this, getString(R.string.location_unavailable), Toast.LENGTH_LONG).show()
        }
        addPharmFab.setOnClickListener {
            userLocation=locationUtils.userLocation
            if (userLocation != null) {
                checkUser()
                val intent = Intent(this, AddPharmacy::class.java)
                intent.putExtra(USER_LOCATION, userLocation)
                startActivity(intent)
            } else Toast.makeText(this, getString(R.string.location_unavailable), Toast.LENGTH_LONG).show()
        }
        profileIV.setOnClickListener {
            val i = Intent(this@MainActivity, ProfileActivity::class.java)
            startActivity(i)
        }
        userLocation=locationUtils.userLocation
        adapter = ResourcesAdapter(resourcesList, userLocation)
    }

    private fun checkUser() {
        val loginResponse = Hawk.get<LoginResponse>(LOGIN_RESPONSE_SHARED)
        calendar = Hawk.get(LOGIN_TIME)
        if (loginResponse != null) {
            token = loginResponse.token
            tokenExp = loginResponse.expire
            checkExpirationLimit(token, tokenExp, getID(), calendar, this)
            getMyResources()
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

    private fun fetchColor(@ColorRes color: Int): Int {
        return ContextCompat.getColor(this, color)
    }

    fun getID(): String {
        return Settings.Secure.getString(this@MainActivity.contentResolver, Settings.Secure.ANDROID_ID)
    }

    fun checkNearestMarker(location: Location?): List<MyResources>? {
        val sortedDoctors = resourcesList
        userLocation=locationUtils.userLocation
        userLocation?.let {
            Collections.sort(sortedDoctors, Comparator<MyResources> { marker1, marker2 ->
                val locationA = Location("point A")
                locationA.latitude = marker1.latitude.toDouble()
                locationA.longitude = marker1.longitude.toDouble()
                val locationB = Location("point B")
                locationB.latitude = marker2.latitude.toDouble()
                locationB.longitude = marker2.longitude.toDouble()
                val distanceOne = it.distanceTo(locationA)
                val distanceTwo = it.distanceTo(locationB)
                return@Comparator java.lang.Float.compare(distanceOne, distanceTwo)
            })
        }
        userLocation=locationUtils.userLocation
        adapter.userLocation = userLocation
        adapter.resources = sortedDoctors
        resourcesList = sortedDoctors
        if (!sortedDoctors.isNullOrEmpty())
            doctorsRV.adapter?.notifyDataSetChanged()
        return sortedDoctors
    }

    fun setRV(sortedDoctors: List<MyResources>) {
//            if (isNear) {
//                imageView4?.setImageResource(R.drawable.ic_my_location_green_24dp)
//                detailsCV?.setOnClickListener {
//                    val intent = Intent(this, FullDetailsActivity::class.java)
//                    startActivity(intent)
//                }
//            } else {
//                imageView4?.setImageResource(R.drawable.ic_my_location_red_24dp)
//                detailsCV?.setOnClickListener(null)
//            }
//        } else imageView4?.setImageResource(R.drawable.ic_not_available)
    }

    private fun getMyResources() {
        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BASE_URL)
            .build()
        val resourcesInterface = retrofit.create(RepresentativesInterface::class.java)
        resourcesInterface.getMyResources(token, getID()).enqueue(object : Callback<MyResourcesResponse> {
            override fun onFailure(call: Call<MyResourcesResponse>, t: Throwable) {
                Toast.makeText(this@MainActivity, t.message, Toast.LENGTH_LONG).show()
            }

            override fun onResponse(call: Call<MyResourcesResponse>, response: Response<MyResourcesResponse>) {
                response.body()?.let {
                    resourcesList = it.Resource
                    userLocation=locationUtils.userLocation
                    if (userLocation != null)
                        checkNearestMarker(null)
                    doctorsRV.adapter = adapter
                    doctorsRV.layoutManager = LinearLayoutManager(this@MainActivity)
                }
            }
        })
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            100 -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        if (PermissionChecker.checkSelfPermission(
                                this,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                                this,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            ) != PackageManager.PERMISSION_GRANTED
                        )
                        else locationUtils.requestUpdates()
                    }
                    Activity.RESULT_CANCELED -> locationUtils.initGPS()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            10 -> {
                if ((grantResults.isNotEmpty()
                            && grantResults[0] == PackageManager.PERMISSION_GRANTED
                            && grantResults[1] == PackageManager.PERMISSION_GRANTED
                            && grantResults[2] == PackageManager.PERMISSION_GRANTED)
                ) {
                    locationUtils.getLastKnowLocation()
                } else {
                    if (permissionCount > 0) {
                        if (!ActivityCompat.shouldShowRequestPermissionRationale(
                                this,
                                Manifest.permission.WRITE_CONTACTS
                            )
                        ) {
                            showMessageOKCancel(getString(R.string.permissionsTitle),
                                getString(R.string.permissionMessage),
                                DialogInterface.OnClickListener { dialog, which -> locationUtils.requestUpdates() },
                                DialogInterface.OnClickListener { dialog, which ->
                                    this.finish()
                                })
                            return
                        }
                    } else {
                        permissionCount++
                    }
                }
                return
            }
            else -> return
        }
    }

    private fun showMessageOK(title: String, message: String, okListener: DialogInterface.OnClickListener) {
        AlertDialog.Builder(this@MainActivity)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(getString(R.string.okDialog), okListener)
            .create()
            .show()
    }

    private fun showMessageOKCancel(
        title: String,
        message: String,
        okListener: DialogInterface.OnClickListener,
        cancelListener: DialogInterface.OnClickListener
    ) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(getString(R.string.okDialog), okListener)
            .setNegativeButton(getString(R.string.cancelDialog), cancelListener)
            .create()
            .show()
    }

    override fun onResume() {
        getMyResources()
        doctorsRV.adapter?.notifyDataSetChanged()
        super.onResume()
    }

    override fun onStop() {
        locationUtils.stopLocation()
        super.onStop()
    }

    override fun onRestart() {
       locationUtils.getLastKnowLocation()
        super.onRestart()
    }
}