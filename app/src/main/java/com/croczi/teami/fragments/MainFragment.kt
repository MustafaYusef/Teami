package com.croczi.teami.fragments


import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.content.PermissionChecker
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.getbase.floatingactionbutton.FloatingActionsMenu

import com.croczi.teami.R
import com.croczi.teami.activities.AddDoctor
import com.croczi.teami.activities.AddPharmacy
import com.croczi.teami.activities.ProfileActivity
import com.croczi.teami.adapters.ResourcesAdapter
import com.croczi.teami.models.*
import com.croczi.teami.retrofit.RepresentativesInterface
import com.croczi.teami.utils.*
import kotlinx.android.synthetic.main.fragment_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import kotlin.Comparator

class MainFragment : Fragment() {

    var resourcesList: List<MyResources>? = null
    private var userLocation: Location? = null
    private var token: String? = null
    private var tokenExp: Long? = 0
    lateinit var adapter: ResourcesAdapter
    private lateinit var locationUtils: LocationUtils
    private var permissionCount = 0
    private lateinit var myContext: Context
    private var loginResponse: LoginResponse? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loginResponse = arguments?.get(Consts.LOGIN_RESPONSE_SHARED) as LoginResponse?
        token = loginResponse?.token
        tokenExp = loginResponse?.expire
        pink_icon.visibility = View.GONE
        context?.let {
            myContext = it
        }
//        context?.let {
        locationUtils = LocationUtils.getInstance(myContext)
        userLocation = locationUtils.userLocation
//        }
        profileIV.setOnClickListener {
            val i = Intent(context, ProfileActivity::class.java)
            startActivity(i)
        }
        userLocation = locationUtils.userLocation
        adapter = ResourcesAdapter(resourcesList, userLocation)
        getMyResources()
        resourcesRefresh.setOnRefreshListener {
            getMyResources()
            emptyListLayout.visibility = View.INVISIBLE
            errorLayout.visibility = View.INVISIBLE
        }
        getUserData(token, getID(context))
    }

    fun getUserData(token: String?, phoneId: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl(Consts.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(RepresentativesInterface::class.java)
            .getMe(MeRequest(token, phoneId)).enqueue(object : Callback<MeResponse> {
                override fun onFailure(call: Call<MeResponse>, t: Throwable) {
                    Toast.makeText(context, "Error getting user info", Toast.LENGTH_LONG).show()

                }

                override fun onResponse(call: Call<MeResponse>, response: Response<MeResponse>) {
                    val meResponse = response.body()
                    setFabs(meResponse)
                }
            })
    }

    fun setFabs(meResponse: MeResponse?) {
        pink_icon?.visibility = View.VISIBLE
        pink_icon?.setOnFloatingActionsMenuUpdateListener(object :
            FloatingActionsMenu.OnFloatingActionsMenuUpdateListener {
            override fun onMenuExpanded() {
                dimView?.visibility = View.VISIBLE
                dimView?.setOnClickListener {
                    pink_icon?.collapse()
                }
            }

            override fun onMenuCollapsed() {
                dimView?.visibility = View.GONE
            }
        })
        addPharmFab?.visibility = View.VISIBLE
        addPharmFab?.setOnClickListener {
            userLocation = locationUtils.userLocation
            if (userLocation != null) {
                val intent = Intent(context, AddPharmacy::class.java)
                intent.putExtra(Consts.USER_LOCATION, userLocation)
                startActivity(intent)
            } else Toast.makeText(context, getString(R.string.location_unavailable), Toast.LENGTH_LONG).show()
        }
        if (meResponse?.user?.Role != "sales_delegate") {
            addDocFab?.setOnClickListener {
                userLocation = locationUtils.userLocation
                if (userLocation != null) {
                    val intent = Intent(context, AddDoctor::class.java)
                    startActivity(intent)
                } else Toast.makeText(context, getString(R.string.location_unavailable), Toast.LENGTH_LONG).show()
            }
        } else addDocFab?.visibility = View.GONE
    }

//    private fun fetchColor(@ColorRes color: Int): Int {
//        return ContextCompat.getColor(view?.context, color)
//    }

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
        val filtered = arrayListOf<MyResources>()
        if (sortedDoctors != null)
            for (i in sortedDoctors) {
                val docLocation = Location("Nearest Doctor")
                docLocation.latitude = i.latitude.toDouble()
                docLocation.longitude = i.longitude.toDouble()

                userLocation?.let {
                    val distance = it.distanceTo(docLocation)
                    if (distance in 0.0..10000.0) {
                        filtered.add(i)
                    }
                }
            }
        userLocation = locationUtils.userLocation
        adapter.userLocation = userLocation
        if (filtered.isNotEmpty()) {
            resourcesRV.visibility = View.VISIBLE
            emptyListLayout.visibility = View.INVISIBLE
            adapter.resources = filtered
        } else {
            resourcesRV.visibility = View.INVISIBLE
            emptyListLayout.visibility = View.VISIBLE
        }
        resourcesList = sortedDoctors
        if (!sortedDoctors.isNullOrEmpty())
            resourcesRV.adapter?.notifyDataSetChanged()
        return sortedDoctors
    }

    fun getMyResources() {
        resourcesRefresh?.isRefreshing = true
        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(Consts.BASE_URL)
            .build()
        val resourcesInterface = retrofit.create(RepresentativesInterface::class.java)
        resourcesInterface.getMyResources(token, getID(context)).enqueue(object :
            Callback<MyResourcesResponse> {
            override fun onFailure(call: Call<MyResourcesResponse>, t: Throwable) {
                resourcesRefresh?.isRefreshing = false
                errorLayout.visibility = View.VISIBLE
                resourcesRV.visibility = View.INVISIBLE
            }

            override fun onResponse(call: Call<MyResourcesResponse>, response: Response<MyResourcesResponse>) {
                resourcesRefresh?.isRefreshing = false
                response.body()?.let {
                    errorLayout?.visibility = View.INVISIBLE
                    resourcesRV?.visibility = View.VISIBLE
                    resourcesList = it.Resource
                    userLocation = locationUtils.userLocation
                    if (userLocation != null)
                        checkNearestMarker()
                    resourcesRV?.adapter = adapter
                    resourcesRV?.layoutManager = LinearLayoutManager(context)
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
                                myContext,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                                myContext,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            ) != PackageManager.PERMISSION_GRANTED
                        )
                        else locationUtils.requestUpdates(myContext)
                    }
                    Activity.RESULT_CANCELED -> locationUtils.initGPS(myContext)
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            10 -> {
                if ((grantResults.isNotEmpty()
                            && grantResults[0] == PackageManager.PERMISSION_GRANTED
                            && grantResults[1] == PackageManager.PERMISSION_GRANTED
                            && grantResults[2] == PackageManager.PERMISSION_GRANTED)
                ) {
                    locationUtils.getLastKnowLocation(myContext)
                } else {
                    if (permissionCount > 0) {
                        if (!ActivityCompat.shouldShowRequestPermissionRationale(
                                myContext as Activity,
                                Manifest.permission.WRITE_CONTACTS
                            )
                        ) {
                            showMessageOKCancel(context as Activity, getString(R.string.permissionsTitle),
                                getString(R.string.permissionMessage),
                                DialogInterface.OnClickListener { dialog, which -> locationUtils.requestUpdates(myContext) },
                                DialogInterface.OnClickListener { dialog, which ->
                                    (myContext as Activity).finish()
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

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        locationUtils.getLastKnowLocation(myContext)
        super.onViewStateRestored(savedInstanceState)
    }
}
