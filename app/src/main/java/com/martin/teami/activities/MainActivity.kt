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
import android.support.v4.content.ContextCompat
import android.support.annotation.ColorRes
import android.support.v4.app.ActivityCompat
import android.support.v4.content.PermissionChecker
import android.view.View
import android.widget.Toast
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.martin.teami.R
import com.martin.teami.models.*
import com.martin.teami.retrofit.RepresentativesInterface
import com.martin.teami.utils.Consts.BASE_URL
import com.martin.teami.utils.Consts.LOGIN_RESPONSE_SHARED
import com.martin.teami.utils.Consts.LOGIN_TIME
import com.martin.teami.utils.Consts.USER_LOCATION
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

    private lateinit var locationManager: LocationManager
    private lateinit var listener: LocationListener
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var doctorsList: List<MyDoctor>
    private lateinit var userLocation: Location
    private var permissionCount = 0
    private lateinit var token: String
    private var tokenExp: Long = 0
    private var calendar: Calendar? = null
    private var running = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Hawk.init(this).build()
        imageView7.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        checkUser()

        locationRequest = LocationRequest()

        locationManager = this.getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager

        initListener()

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        getLastKnowLocation()

        requestUpdates()

//        getMyResources()
        addDocFab.setOnClickListener {
            if (this::userLocation.isInitialized) {
                    checkUser()
                    val intent = Intent(this, AddDoctor::class.java)
                    intent.putExtra(USER_LOCATION, userLocation)
                    startActivity(intent)
            } else Toast.makeText(this, getString(R.string.location_unavailable), Toast.LENGTH_LONG).show()
        }
        addPharmFab.setOnClickListener {
            if (this::userLocation.isInitialized) {
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

    fun checkNearestMarker(location: Location?): List<Doctor> {
        val sortedMarkers = doctorsList
        Collections.sort(sortedMarkers, Comparator<Doctor> { marker1, marker2 ->
            val locationA = Location("point A")
            locationA.latitude = marker1.latitude.toDouble()
            locationA.longitude = marker1.longitude.toDouble()
            val locationB = Location("point B")
            locationB.latitude = marker2.latitude.toDouble()
            locationB.longitude = marker2.longitude.toDouble()
            val distanceOne = userLocation.distanceTo(locationA)
            val distanceTwo = userLocation.distanceTo(locationB)
            return@Comparator java.lang.Float.compare(distanceOne, distanceTwo)
        })
        setCardView(checkIfNearMarker(sortedMarkers))
        return sortedMarkers
    }

    fun checkIfNearMarker(sortedMarkers: List<Doctor>): Boolean {

        val nearestPharm = Location("Nearest Doctor")
        nearestPharm.latitude = sortedMarkers[0].latitude.toDouble()
        nearestPharm.longitude = sortedMarkers[0].longitude.toDouble()
        val distance = userLocation.distanceTo(nearestPharm)
        return distance < 10
    }

    fun setCardView(isNear: Boolean) {
        if (this::userLocation.isInitialized) {
            if (isNear) {
                imageView4?.setImageResource(R.drawable.ic_my_location_green_24dp)
                detailsCV?.setOnClickListener {
                    val intent = Intent(this, FullDetailsActivity::class.java)
                    startActivity(intent)
                }
            } else {
                imageView4?.setImageResource(R.drawable.ic_my_location_red_24dp)
                detailsCV?.setOnClickListener(null)
            }
        } else imageView4?.setImageResource(R.drawable.ic_not_available)
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
                    doctorsList = it.Resource.doctors
//                    if (this@MainActivity::userLocation.isInitialized)
//                        checkNearestMarker(null)
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
                        else requestUpdates()
                    }
                    Activity.RESULT_CANCELED -> initGPS()
                }
            }
        }
    }

    private fun initGPS() {
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        locationRequest.interval = 16000
        locationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        locationRequest.maxWaitTime = 32000

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
//                    if (this@MainActivity::doctorsList.isInitialized)
//                        checkNearestMarker(it)
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
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 5f, listener)
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5f, listener)
        locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 5000, 5f, listener)
        if (fusedLocationProviderClient.locationAvailability.isSuccessful)
            fusedLocationProviderClient.lastLocation.addOnCompleteListener { task ->
                val location = task.result
                val name = this.intent.getStringExtra("NAME")
                location?.let {
                    userLocation = it
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

    override fun onStop() {
        running = false
        super.onStop()
    }

    override fun onStart() {
        running = true
        super.onStart()
    }
}
