package com.martin.teami.activities

import android.Manifest
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
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
import com.martin.teami.utils.*
import com.martin.teami.utils.Consts.BASE_URL
import com.martin.teami.utils.Consts.USER_LOCATION
import com.orhanobut.hawk.Hawk
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*


class MainActivity : AppCompatActivity() {

    var resourcesList: List<MyResources>? = null
    private var userLocation: Location? = null
    private lateinit var token: String
    private var tokenExp: Long = 0
    lateinit var adapter: ResourcesAdapter
    private lateinit var locationUtils: LocationUtils
    private var permissionCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Hawk.init(this).build()
        pink_icon.visibility = View.GONE
        val loginResponse = checkUser(this)
        if (loginResponse != null) {
            token = loginResponse.token
            tokenExp = loginResponse.expire
        }
        locationUtils = LocationUtils(this@MainActivity)
        locationUtils.initLocation()
        userLocation = locationUtils.userLocation

        profileIV.setOnClickListener {
            val i = Intent(this@MainActivity, ProfileActivity::class.java)
            startActivity(i)
        }

        userLocation = locationUtils.userLocation
        adapter = ResourcesAdapter(resourcesList, userLocation)
        getMyResources()
        resourcesRefresh.setOnRefreshListener {
            getMyResources()
            emptyListLayout.visibility = View.INVISIBLE
        }
        getUserData(token, getID(this))
    }

    fun getUserData(token: String, phoneId: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(RepresentativesInterface::class.java)
            .getMe(MeRequest(token, phoneId)).enqueue(object : Callback<MeResponse> {
                override fun onFailure(call: Call<MeResponse>, t: Throwable) {
                }

                override fun onResponse(call: Call<MeResponse>, response: Response<MeResponse>) {
                    val meResponse = response.body()
                    setFabs(meResponse)
                }
            })
    }

    fun setFabs(meResponse: MeResponse?) {
        pink_icon.visibility=View.VISIBLE
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

        if (meResponse?.user?.Role != "sales_delegate") {
            addPharmFab.visibility=View.GONE
            addDocFab.setOnClickListener {
                userLocation = locationUtils.userLocation
                if (userLocation != null) {
                    checkUser(this)
                    val intent = Intent(this, AddDoctor::class.java)
                    startActivity(intent)
                } else Toast.makeText(this, getString(R.string.location_unavailable), Toast.LENGTH_LONG).show()
            }
        } else {
            addDocFab.visibility=View.GONE
            addPharmFab.setOnClickListener {
                userLocation = locationUtils.userLocation
                if (userLocation != null) {
                    checkUser(this)
                    val intent = Intent(this, AddPharmacy::class.java)
                    intent.putExtra(USER_LOCATION, userLocation)
                    startActivity(intent)
                } else Toast.makeText(this, getString(R.string.location_unavailable), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun fetchColor(@ColorRes color: Int): Int {
        return ContextCompat.getColor(this, color)
    }

    fun checkNearestMarker(): List<MyResources>? {
        val sortedDoctors = resourcesList
        userLocation = locationUtils.userLocation
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
        userLocation = locationUtils.userLocation
        adapter.userLocation = userLocation
        adapter.resources = sortedDoctors
        resourcesList = sortedDoctors
        if (!sortedDoctors.isNullOrEmpty())
            resourcesRV.adapter?.notifyDataSetChanged()
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

    fun getMyResources() {
        resourcesRefresh.isRefreshing = true
        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BASE_URL)
            .build()
        val resourcesInterface = retrofit.create(RepresentativesInterface::class.java)
        resourcesInterface.getMyResources(token, getID(this)).enqueue(object : Callback<MyResourcesResponse> {
            override fun onFailure(call: Call<MyResourcesResponse>, t: Throwable) {
                Toast.makeText(this@MainActivity, t.message, Toast.LENGTH_LONG).show()
                resourcesRefresh.isRefreshing = false
                emptyListLayout.visibility = View.VISIBLE
                resourcesRV.visibility = View.INVISIBLE
            }

            override fun onResponse(call: Call<MyResourcesResponse>, response: Response<MyResourcesResponse>) {
                resourcesRefresh.isRefreshing = false
                response.body()?.let {
                    emptyListLayout.visibility = View.INVISIBLE
                    resourcesRV.visibility = View.VISIBLE
                    resourcesList = it.Resource
                    userLocation = locationUtils.userLocation
                    if (userLocation != null)
                        checkNearestMarker()
                    resourcesRV.adapter = adapter
                    resourcesRV.layoutManager = LinearLayoutManager(this@MainActivity)
                    adapter.notifyDataSetChanged()
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
                            showMessageOKCancel(this@MainActivity, getString(R.string.permissionsTitle),
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

    override fun onResume() {
        getMyResources()
        resourcesRV.adapter?.notifyDataSetChanged()
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
