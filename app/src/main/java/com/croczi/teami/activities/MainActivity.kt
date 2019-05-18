package com.croczi.teami.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.croczi.teami.R
import com.croczi.teami.fragments.LoginFragment
import com.croczi.teami.fragments.MainFragment
import com.croczi.teami.models.*
import com.croczi.teami.utils.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import com.orhanobut.hawk.Hawk
import com.google.firebase.iid.InstanceIdResult
import com.google.android.gms.tasks.OnSuccessListener
import android.app.Activity


class MainActivity : AppCompatActivity() {
    //
//    var resourcesList: List<MyResources>? = null
//    private var userLocation: Location? = null
    private var token: String? = null
    private var tokenExp: Long? = 0
    //    lateinit var adapter: ResourcesAdapter
//    private lateinit var locationUtils: LocationUtils
//    private var permissionCount = 0
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
//        pink_icon.visibility = View.GONE
        getLoginResponse()
        if (intent.getBooleanExtra("logout", false))
            gotoMain()
        else
            checkUser(this)
        //            gotoLogin()
//        locationUtils = LocationUtils(this@MainActivity)
//        locationUtils.initLocation()
//        userLocation = locationUtils.userLocation

//        profileIV.setOnClickListener {
//            val i = Intent(this@MainActivity, ProfileActivity::class.java)
//            startActivity(i)
//        }

//        userLocation = locationUtils.userLocation
//        adapter = ResourcesAdapter(resourcesList, userLocation)
//        getMyResources()
//        resourcesRefresh.setOnRefreshListener {
//            getMyResources()
//            emptyListLayout.visibility = View.INVISIBLE
//            errorLayout.visibility = View.INVISIBLE
//        }
//        getUserData(token, getID(this))
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

//    fun getUserData(token: String, phoneId: String) {
//        val retrofit = Retrofit.Builder()
//            .baseUrl(BASE_URL)
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//        retrofit.create(RepresentativesInterface::class.java)
//            .getMe(MeRequest(token, phoneId)).enqueue(object : Callback<MeResponse> {
//                override fun onFailure(call: Call<MeResponse>, t: Throwable) {
//                    Toast.makeText(this@MainActivity, "Error getting user info", Toast.LENGTH_LONG).show()
//                }
//
//                override fun onResponse(call: Call<MeResponse>, response: Response<MeResponse>) {
//                    val meResponse = response.body()
//                    setFabs(meResponse)
//                }
//            })
//    }
//
//    fun setFabs(meResponse: MeResponse?) {
//        pink_icon.visibility = View.VISIBLE
//        pink_icon.setOnFloatingActionsMenuUpdateListener(object :
//            FloatingActionsMenu.OnFloatingActionsMenuUpdateListener {
//            override fun onMenuExpanded() {
//                dimView.visibility = View.VISIBLE
//                dimView.setOnClickListener {
//                    pink_icon.collapse()
//                }
//            }
//
//            override fun onMenuCollapsed() {
//                dimView.visibility = View.GONE
//            }
//        })
//        addPharmFab.visibility = View.VISIBLE
//        addPharmFab.setOnClickListener {
//            userLocation = locationUtils.userLocation
//            if (userLocation != null) {
//                checkUser(this)
//                val intent = Intent(this, AddPharmacy::class.java)
//                intent.putExtra(USER_LOCATION, userLocation)
//                startActivity(intent)
//            } else Toast.makeText(this, getString(R.string.location_unavailable), Toast.LENGTH_LONG).show()
//        }
//        if (meResponse?.user?.Role != "sales_delegate") {
//            addDocFab.setOnClickListener {
//                userLocation = locationUtils.userLocation
//                if (userLocation != null) {
//                    checkUser(this)
//                    val intent = Intent(this, AddDoctor::class.java)
//                    startActivity(intent)
//                } else Toast.makeText(this, getString(R.string.location_unavailable), Toast.LENGTH_LONG).show()
//            }
//        } else addDocFab.visibility = View.GONE
//    }
//
//    private fun fetchColor(@ColorRes color: Int): Int {
//        return ContextCompat.getColor(this, color)
//    }
//
//    fun checkNearestMarker(): List<MyResources>? {
//        val sortedDoctors = resourcesList
//        userLocation = locationUtils.userLocation
//        userLocation?.let {
//            Collections.sort(sortedDoctors, Comparator<MyResources> { marker1, marker2 ->
//                val locationA = Location("point A")
//                locationA.latitude = marker1.latitude.toDouble()
//                locationA.longitude = marker1.longitude.toDouble()
//                val locationB = Location("point B")
//                locationB.latitude = marker2.latitude.toDouble()
//                locationB.longitude = marker2.longitude.toDouble()
//                val distanceOne = it.distanceTo(locationA)
//                val distanceTwo = it.distanceTo(locationB)
//                return@Comparator java.lang.Float.compare(distanceOne, distanceTwo)
//            })
//        }
//        val filtered = arrayListOf<MyResources>()
//        if (sortedDoctors != null)
//            for (i in sortedDoctors) {
//                val docLocation = Location("Nearest Doctor")
//                docLocation.latitude = i.latitude.toDouble()
//                docLocation.longitude = i.longitude.toDouble()
//
//                userLocation?.let {
//                    val distance = it.distanceTo(docLocation)
//                    if (distance in 0.0..10000.0) {
//                        filtered.add(i)
//                    }
//                }
//            }
//        userLocation = locationUtils.userLocation
//        adapter.userLocation = userLocation
//        if (filtered.isNotEmpty()) {
//            resourcesRV.visibility = View.VISIBLE
//            emptyListLayout.visibility = View.INVISIBLE
//            adapter.resources = filtered
//        } else {
//            resourcesRV.visibility = View.INVISIBLE
//            emptyListLayout.visibility = View.VISIBLE
//        }
//        resourcesList = sortedDoctors
//        if (!sortedDoctors.isNullOrEmpty())
//            resourcesRV.adapter?.notifyDataSetChanged()
//        return sortedDoctors
//    }
//
//    fun getMyResources() {
//        resourcesRefresh.isRefreshing = true
//        val retrofit = Retrofit.Builder()
//            .addConverterFactory(GsonConverterFactory.create())
//            .baseUrl(BASE_URL)
//            .build()
//        val resourcesInterface = retrofit.create(RepresentativesInterface::class.java)
//        resourcesInterface.getMyResources(token, getID(this)).enqueue(object : Callback<MyResourcesResponse> {
//            override fun onFailure(call: Call<MyResourcesResponse>, t: Throwable) {
//                resourcesRefresh.isRefreshing = false
//                errorLayout.visibility = View.VISIBLE
//                resourcesRV.visibility = View.INVISIBLE
//            }
//
//            override fun onResponse(call: Call<MyResourcesResponse>, response: Response<MyResourcesResponse>) {
//                resourcesRefresh.isRefreshing = false
//                response.body()?.let {
//                    errorLayout.visibility = View.INVISIBLE
//                    resourcesRV.visibility = View.VISIBLE
//                    resourcesList = it.Resource
//                    userLocation = locationUtils.userLocation
//                    if (userLocation != null)
//                        checkNearestMarker()
//                    resourcesRV.adapter = adapter
//                    resourcesRV.layoutManager = LinearLayoutManager(this@MainActivity)
//                    adapter.notifyDataSetChanged()
//                }
//            }
//        })
//    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        when (requestCode) {
//            100 -> {
//                when (resultCode) {
//                    Activity.RESULT_OK -> {
//                        if (PermissionChecker.checkSelfPermission(
//                                this,
//                                Manifest.permission.ACCESS_FINE_LOCATION
//                            ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
//                                this,
//                                Manifest.permission.ACCESS_COARSE_LOCATION
//                            ) != PackageManager.PERMISSION_GRANTED
//                        )
//                        else locationUtils.requestUpdates()
//                    }
//                    Activity.RESULT_CANCELED -> locationUtils.initGPS()
//                }
//            }
//        }
//    }
//
//
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//        when (requestCode) {
//            10 -> {
//                if ((grantResults.isNotEmpty()
//                            && grantResults[0] == PackageManager.PERMISSION_GRANTED
//                            && grantResults[1] == PackageManager.PERMISSION_GRANTED
//                            && grantResults[2] == PackageManager.PERMISSION_GRANTED)
//                ) {
//                    locationUtils.getLastKnowLocation()
//                } else {
//                    if (permissionCount > 0) {
//                        if (!ActivityCompat.shouldShowRequestPermissionRationale(
//                                this,
//                                Manifest.permission.WRITE_CONTACTS
//                            )
//                        ) {
//                            showMessageOKCancel(this@MainActivity, getString(R.string.permissionsTitle),
//                                getString(R.string.permissionMessage),
//                                DialogInterface.OnClickListener { dialog, which -> locationUtils.requestUpdates() },
//                                DialogInterface.OnClickListener { dialog, which ->
//                                    this.finish()
//                                })
//                            return
//                        }
//                    } else {
//                        permissionCount++
//                    }
//                }
//                return
//            }
//            else -> return
//        }
//    }
//
//    override fun onResume() {
//        getMyResources()
//        resourcesRV.adapter?.notifyDataSetChanged()
//        super.onResume()
//    }
//
//    override fun onStop() {
//        locationUtils.stopLocation()
//        super.onStop()
//    }
//
//    override fun onRestart() {
//        locationUtils.getLastKnowLocation()
//        super.onRestart()
//    }
}
