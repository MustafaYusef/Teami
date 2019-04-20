package com.martin.teami.utils

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.support.v4.content.PermissionChecker
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.martin.teami.R
import com.martin.teami.activities.MainActivity
import com.martin.teami.adapters.ResourcesAdapter
import com.martin.teami.models.*
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class LocationUtils(var context: Context) {

    private var activity = context as Activity
    private lateinit var locationManager: LocationManager
    private lateinit var listener: LocationListener
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest

    var userLocation: Location? = null

    fun initLocation() {

        locationRequest = LocationRequest()

        locationManager = context.getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager

        initListener()

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

        getLastKnowLocation()

        requestUpdates()
    }

    fun getID(): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    fun initGPS() {
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
                                    activity,
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
                    LocationServices.getFusedLocationProviderClient(context)
                getLastKnowLocation()
                location?.let {
                    userLocation = it
                    val userLat = location.latitude
                    val userLong = location.longitude
                    val userLatLng = LatLng(userLat, userLong)
                }
                if (activity is MainActivity) {
                    if ((activity as MainActivity).resourcesList == null) {
                        (activity as MainActivity).getMyResources()
                    }else {
                        (activity as MainActivity).adapter.notifyDataSetChanged()
                        (activity as MainActivity).checkNearestMarker()
                    }
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

    fun requestUpdates() {
        if (PermissionChecker.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && PermissionChecker.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                activity.requestPermissions(
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

    fun getLastKnowLocation() {
        if (PermissionChecker.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && PermissionChecker.checkSelfPermission(
                context,
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
                val name = activity.intent.getStringExtra("NAME")
                location?.let {
                    userLocation = it
                }
            }
    }

    fun stopLocation() {
        locationManager.removeUpdates(listener)
    }
}