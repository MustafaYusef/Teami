package com.martin.teami.activities

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem
import kotlinx.android.synthetic.main.activity_main.*
import android.support.v4.content.ContextCompat
import android.support.annotation.ColorRes
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.PermissionChecker
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.martin.teami.fragments.HomeFragment
import com.martin.teami.fragments.MapFragment
import com.martin.teami.R
import com.martin.teami.adapters.BottomBarAdapter
import com.martin.teami.adapters.PharmaciesAdapter
import com.martin.teami.models.LoginResponse
import com.martin.teami.models.PharmaciesResponse
import com.martin.teami.models.Pharmacy
import com.martin.teami.models.RegisterResponse
import com.martin.teami.retrofit.RepresentativesInterface
import com.martin.teami.utils.Consts
import com.martin.teami.utils.Consts.LOGIN_RESPONSE_SHARED
import com.martin.teami.utils.Consts.LOGIN_TIME
import com.martin.teami.utils.checkExpirationLimit
import com.martin.teami.utils.logoutUser
import com.orhanobut.hawk.Hawk
import kotlinx.android.synthetic.main.activity_map.*
import kotlinx.android.synthetic.main.map_bottom_sheet_layout.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.StringBuilder
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var locationManager: LocationManager
    private lateinit var listener: LocationListener
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var markersList: List<Pharmacy>
    private lateinit var userLocation: Location
    private var permissionCount = 0
    private lateinit var token: String
    private var tokenExp: Int = 0
    private var calendar: Calendar? = null
    private val homeFragment = HomeFragment()
    private val mapFragment = MapFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Hawk.init(this).build()

        checkUser()
        setBottomNav()

        locationRequest = LocationRequest()

        locationManager = this.getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager

        initListener()

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        getLastKnowLocation()

        requestUpdates()

        getMarkers()

    }

    private fun checkUser() {
                val loginResponse = Hawk.get<LoginResponse>(LOGIN_RESPONSE_SHARED)
        calendar = Hawk.get(LOGIN_TIME)
        if (loginResponse != null) {
            token = loginResponse.token
            tokenExp = loginResponse.expire
            checkExpirationLimit(token, tokenExp, getID(), calendar, this)
        } else {
            val intent = Intent(this, LoginActivity::class.java)
            this.finish()
            startActivity(intent)
        }
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
                checkExpirationLimit(token, tokenExp, getID(), calendar, this@MainActivity)
                viewPager.currentItem = position
            }
            return@setOnTabSelectedListener true
        }

        val bundle = Bundle()
        if (this::token.isInitialized) {
            bundle.putString("TOKEN", token)
            bundle.putInt("EXP",tokenExp)
            bundle.putString("PHONEID", getID())
            homeFragment.arguments = bundle
        } else {
            val intent = Intent(this, LoginActivity::class.java)
            this.finish()
            startActivity(intent)
        }
        val fragmentsList = listOf(
            homeFragment,
            mapFragment
        )
        val pagerAdapter = BottomBarAdapter(supportFragmentManager, fragmentsList)
        viewPager.adapter = pagerAdapter
        viewPager.setPagingEnabled(false)
        bottomNavigation.currentItem = 0
    }

    private fun fetchColor(@ColorRes color: Int): Int {
        return ContextCompat.getColor(this, color)
    }

    fun getID(): String {
        return Settings.Secure.getString(this@MainActivity.contentResolver, Settings.Secure.ANDROID_ID)
    }

    fun checkNearestMarker(location: Location?): List<Pharmacy> {
        val sortedMarkers = markersList
        Collections.sort(sortedMarkers, Comparator<Pharmacy> { marker1, marker2 ->
            val locationA = Location("point A")
            locationA.latitude = marker1.latitude
            locationA.longitude = marker1.longitude
            val locationB = Location("point B")
            locationB.latitude = marker2.latitude
            locationB.longitude = marker2.longitude
            val distanceOne = userLocation.distanceTo(locationA)
            val distanceTwo = userLocation.distanceTo(locationB)
            return@Comparator java.lang.Float.compare(distanceOne, distanceTwo)
        })
        mapFragment.listMarkers(sortedMarkers, location ?: userLocation)
        homeFragment.setCardView(checkIfNearMarker(sortedMarkers))
//        getMarkers(location ?: userLocation)
        return sortedMarkers
    }

    fun checkIfNearMarker(sortedMarkers: List<Pharmacy>): Boolean {

        val nearestPharm = Location("Nearest Pharmacy")
        nearestPharm.latitude = sortedMarkers[0].latitude
        nearestPharm.longitude = sortedMarkers[0].longitude
        val distance = userLocation.distanceTo(nearestPharm)
        return distance < 10
    }

    private fun registerLocation(marker: Pharmacy) {
        marker.registered = true
        val retrofit = Retrofit.Builder()
            .baseUrl(Consts.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val registerInterface = retrofit.create(RepresentativesInterface::class.java)
        val registerCall = registerInterface.registerPharmacy(
            "application/json", "no-cache", true,
            marker
        )
        registerCall.enqueue(object : Callback<RegisterResponse> {
            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {

            }

            override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
            }

        })

    }

    private fun getMarkers() {
        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://pharmas.herokuapp.com/")
            .build()
        val pharmInterface = retrofit.create(RepresentativesInterface::class.java)
        pharmInterface.getPharmacies().enqueue(object : Callback<PharmaciesResponse> {
            override fun onFailure(call: Call<PharmaciesResponse>, t: Throwable) {
                Toast.makeText(this@MainActivity, t.message, Toast.LENGTH_LONG).show()
            }

            override fun onResponse(call: Call<PharmaciesResponse>, response: Response<PharmaciesResponse>) {
                response.body()?.let {
                    markersList = it.pharmacies
                    if (this@MainActivity::userLocation.isInitialized)
                        checkNearestMarker(null)
                    mapFragment.showMarkers(it.pharmacies)
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
                        mapView?.getMapAsync {
                            if (PermissionChecker.checkSelfPermission(
                                    this,
                                    Manifest.permission.ACCESS_FINE_LOCATION
                                ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                                    this,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                ) != PackageManager.PERMISSION_GRANTED
                            )
                                it.isMyLocationEnabled = true
                            else requestUpdates()
                        }
                    }
                    Activity.RESULT_CANCELED -> initGPS()
                }
            }
        }
    }

    private fun initGPS() {
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        val result = LocationServices.getSettingsClient(this@MainActivity)
            .checkLocationSettings(builder.build())

        result.addOnCompleteListener(object : OnCompleteListener<LocationSettingsResponse> {
            override fun onComplete(task: Task<LocationSettingsResponse>) {
                try {
                    var response = task.getResult(ApiException::class.java)
                    // All location settings are satisfied. The client can initialize location
                    // requests here.
                } catch (exception: ApiException) {
                    when (exception.statusCode) {
                        LocationSettingsStatusCodes.RESOLUTION_REQUIRED ->
                            // Location settings are not satisfied. But could be fixed by showing the
                            // user a dialog.
                            try {
                                // Cast to a resolvable exception.
                                val resolvable = exception as ResolvableApiException
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                resolvable.startResolutionForResult(
                                    this@MainActivity,
                                    100
                                )
                            } catch (
                                e: IntentSender.SendIntentException
                            ) {
                                // Ignore the error.
                            } catch (e: ClassCastException) {
                                // Ignore, should be an impossible error.
                            }
                        LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE ->
                            // Location settings are not satisfied. However, we have no way to fix the
                            // settings so we won't show the dialog.
                            return
                    }
                }
            }
        })
    }

    private fun initListener() {
        listener = object : LocationListener {
            override fun onLocationChanged(location: Location?) {
                fusedLocationProviderClient =
                    LocationServices.getFusedLocationProviderClient(this@MainActivity)
                getLastKnowLocation()
                location?.let {
                    userLocation = it
                    val userLat = location.latitude
                    val userLong = location.longitude
                    val userLatLng = LatLng(userLat, userLong)
                    if (this@MainActivity::markersList.isInitialized)
                        checkNearestMarker(it)
                }
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
            }

            override fun onProviderEnabled(provider: String?) {
            }

            override fun onProviderDisabled(provider: String?) {
                initGPS()
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
                    getLastKnowLocation()
                } else {
                    if (permissionCount > 0) {
                        if (!ActivityCompat.shouldShowRequestPermissionRationale(
                                this,
                                Manifest.permission.WRITE_CONTACTS
                            )
                        ) {
                            showMessageOKCancel(getString(R.string.permissionsTitle),
                                getString(R.string.permissionMessage),
                                DialogInterface.OnClickListener { dialog, which -> requestUpdates() },
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

    private fun requestUpdates() {
        if (PermissionChecker.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && PermissionChecker.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(
                    arrayOf(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.INTERNET
                    )
                    , 10
                )
            }
        }
    }

    private fun getLastKnowLocation() {
        if (PermissionChecker.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && PermissionChecker.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestUpdates()
            return
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0f, listener)
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0f, listener)
        locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 5000, 0f, listener)
        fusedLocationProviderClient.lastLocation.addOnCompleteListener { task ->
            val location = task.result
            val name = this.intent.getStringExtra("NAME")
            location?.let {
                userLocation = it
            }
            mapView?.getMapAsync {
                it.isMyLocationEnabled = true
//                getMarkers(location)
            }
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.logout -> logoutUser(this, token, getID())
        }
        return super.onOptionsItemSelected(item)
    }
}
