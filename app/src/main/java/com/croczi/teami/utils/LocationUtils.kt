package com.croczi.teami.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat.requestPermissions
import android.support.v4.app.Fragment
import android.support.v4.content.PermissionChecker
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task


object LocationUtils {
    private lateinit var fragment: Fragment
    private var isActivity: Boolean = true
    private var dialogCount = 0
    private lateinit var locationManager: LocationManager
    private lateinit var listener: LocationListener
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest

    var userLocation: Location? = null

    fun getInstance(context: Context): LocationUtils {

        initLocation(context)

        return LocationUtils
    }

    fun getInstance(fragment: Fragment, context: Context): LocationUtils {
        this.isActivity=false
        this.fragment = fragment
        initLocation(context)

        return LocationUtils
    }

    fun initLocation(context: Context) {

        locationRequest = LocationRequest()

        locationManager = context.getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager

        initListener(context)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

        getLastKnowLocation(context)

        requestUpdates(context, isActivity)
    }

//    fun getID(context: Context): String {
//        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
//    }

    fun initGPS(context: Context) {
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        locationRequest.interval = 3000
        locationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        locationRequest.maxWaitTime = 7000

        val result = LocationServices.getSettingsClient(context)
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
                                    context as Activity,
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

    private fun initListener(context: Context) {
        listener = object : LocationListener {
            override fun onLocationChanged(location: Location?) {
                fusedLocationProviderClient =
                    LocationServices.getFusedLocationProviderClient(context)
                getLastKnowLocation(context)
                location?.let {
                    userLocation = it
                    val userLat = location.latitude
                    val userLong = location.longitude
                    val userLatLng = LatLng(userLat, userLong)
                }
//                if (activity is MainActivity) {
//                    if ((activity as MainActivity).resourcesList == null) {
//                        (activity as MainActivity).getMyResources()
//                    }else {
//                    (activity as MainActivity).checkNearestMarker()
//                    (activity as MainActivity).adapter.notifyDataSetChanged()
//                }
//            }
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
            }

            override fun onProviderEnabled(provider: String?) {
            }

            override fun onProviderDisabled(provider: String?) {
                if (provider == "gps" && dialogCount == 0) {
                    initGPS(context)
                    dialogCount++
                }
            }
        }
    }

    fun requestUpdates(context: Context, isActivity: Boolean) {
        this.isActivity = isActivity
        if (isActivity) {
            if (PermissionChecker.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && PermissionChecker.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    (context as Activity).requestPermissions(
                        arrayOf(
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.INTERNET
                        )
                        , 10
                    )
                }
            }
        } else {
            if (PermissionChecker.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && PermissionChecker.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && this::fragment.isInitialized) {
                    fragment.requestPermissions(
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
    }

    fun getLastKnowLocation(context: Context) {
        if (PermissionChecker.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && PermissionChecker.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestUpdates(context, isActivity)
            return
        }

        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 5f, listener)
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5f, listener)
        locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 5000, 5f, listener)
        if (fusedLocationProviderClient.locationAvailability.isSuccessful)
            fusedLocationProviderClient.lastLocation.addOnCompleteListener { task ->
                val location = task.result
                val name = (context as Activity).intent.getStringExtra("NAME")
                location?.let {
                    userLocation = it
                }
            }
    }

    fun stopLocation() {
        locationManager.removeUpdates(listener)
    }
}