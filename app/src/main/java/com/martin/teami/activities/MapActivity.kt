package com.martin.teami.activities

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.annotation.DrawableRes
import android.support.design.widget.BottomSheetBehavior
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.widget.Toast
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_map.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.martin.teami.utils.Consts.BASE_URL
import com.martin.teami.utils.Consts.MAPVIEW_BUNDLE_KEY
import com.martin.teami.retrofit.RepresentativesInterface
import com.martin.teami.R
import com.martin.teami.adapters.PharmaciesAdapter
import com.martin.teami.models.*
import kotlinx.android.synthetic.main.map_bottom_sheet_layout.*
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.StringBuilder
import java.util.*


class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var locationManager: LocationManager
    private lateinit var listener: LocationListener
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var pharmaciesList: List<Pharmacy>
    private lateinit var userLocation: Location


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        val bottomSheetBehavior = BottomSheetBehavior.from(bottom_sheet)

        locationRequest = LocationRequest()

        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        initListener()

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this@MapActivity)

        initGoogleMaps(savedInstanceState)

        getLastKnowLocation()

        requestUpdates()
    }


    private fun checkNearestPharm(location: Location?) {
        val sortedPharms = pharmaciesList
        Collections.sort(sortedPharms, Comparator<Pharmacy> { pharmacy1, pharmacy2 ->
            val locationA = Location("point A")
            locationA.latitude = pharmacy1.latitude
            locationA.longitude = pharmacy1.longitude
            val locationB = Location("point B")
            locationB.latitude = pharmacy2.latitude
            locationB.longitude = pharmacy2.longitude
            val distanceOne = userLocation.distanceTo(locationA)
            val distanceTwo = userLocation.distanceTo(locationB)
            return@Comparator java.lang.Float.compare(distanceOne, distanceTwo)
        })
        listPharmacies(sortedPharms, location)
        nearestPharmTV.text = "Closest Pharmacy: ${sortedPharms[0].name}"
        registerLocationBtn.setOnClickListener {
            checkIfNearPharm(sortedPharms)
            mapView.getMapAsync {
            getPharmacies(it,location)
            }
        }
    }

    private fun checkIfNearPharm(sortedPharms: List<Pharmacy>) {

        val nearestPharm = Location("Nearest Pharmacy")
        nearestPharm.latitude = sortedPharms[0].latitude
        nearestPharm.longitude = sortedPharms[0].longitude
        val distance = userLocation.distanceTo(nearestPharm)
        if (distance < 50) {
            registerLocation(sortedPharms[0])
            val stringBuilder: StringBuilder = StringBuilder().append(getString(R.string.registeration_done))
                .append(" ${sortedPharms[0].name}")
            val builder: AlertDialog = AlertDialog.Builder(this).setTitle("Done!")
                .setMessage(stringBuilder)
                .setPositiveButton(
                    "Ok"
                ) { dialog, which ->
                    dialog?.dismiss()
                }.show()
        } else {
            val builder: AlertDialog = AlertDialog.Builder(this)
                .setMessage(getString(R.string.not_near_pharmacy))
                .setPositiveButton(
                    "Ok"
                ) { dialog, which -> dialog?.dismiss() }.show()
        }
    }

    private fun registerLocation(pharmacy: Pharmacy) {
        pharmacy.registered = true
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val registerInterface = retrofit.create(RepresentativesInterface::class.java)
        val registerCall = registerInterface.registerPharmacy(
            "application/json", "no-cache", true,
            pharmacy
        )
        registerCall.enqueue(object : Callback<RegisterResponse> {
            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {

            }

            override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                Log.d("Martin", response.body().toString())
                Log.d("Martin", response.message())
            }

        })

    }

    private fun getPharmacies(map: GoogleMap, location: Location?) {
        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BASE_URL)
            .build()
        val pharmInterface = retrofit.create(RepresentativesInterface::class.java)
        pharmInterface.getPharmacies().enqueue(object : Callback<PharmaciesResponse> {
            override fun onFailure(call: Call<PharmaciesResponse>, t: Throwable) {
                Toast.makeText(this@MapActivity, t.message, Toast.LENGTH_LONG).show()
            }

            override fun onResponse(call: Call<PharmaciesResponse>, response: Response<PharmaciesResponse>) {
                response.body()?.let {
                    pharmaciesList = it.pharmacies
                    if (this@MapActivity::userLocation.isInitialized)
                        checkNearestPharm(location)
                    showPharmacies(it.pharmacies, map)
                }
            }
        })
    }

    private fun listPharmacies(pharmacies: List<Pharmacy>, location: Location?) {
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        pharmaciesRV.layoutManager = linearLayoutManager
        pharmaciesRV.adapter = PharmaciesAdapter(pharmacies, location)
    }

    private fun showPharmacies(it: List<Pharmacy>, map: GoogleMap) {
        map.clear()
        it.forEach {
            map.addMarker(MarkerOptions().position(it.getPos()).title(it.name))
                .setIcon(
                    bitmapDescriptorFromVector(
                        this@MapActivity,
                        R.drawable.ic_marker_pharmacy
                    )
                )
        }
    }

    private fun bitmapDescriptorFromVector(context: Context, @DrawableRes vectorDrawableResourceId: Int): BitmapDescriptor {
        val vectorDrawable = ContextCompat.getDrawable(context, vectorDrawableResourceId)!!
        vectorDrawable.setBounds(0, 0, vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight)
        val bitmap =
            Bitmap.createBitmap(vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }


    override fun onMapReady(map: GoogleMap) {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        map.isMyLocationEnabled = true
        map.moveCamera(CameraUpdateFactory.newLatLng(LatLng(33.3152, 44.3661)))
        map.animateCamera(CameraUpdateFactory.zoomTo(11.0f))

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("Martin", "onActivityResult: called.")
        when (requestCode) {
            100 -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        mapView.getMapAsync {
                            if (ContextCompat.checkSelfPermission(
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
        val result = LocationServices.getSettingsClient(this@MapActivity)
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
                                    this@MapActivity,
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
                fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this@MapActivity)
                getLastKnowLocation()
//                mapView.getMapAsync {
//                    getPharmacies(it)
//                }
                location?.let {
                    userLocation = it
                    val userLat = location.latitude
                    val userLong = location.longitude
                    val userLatLng = LatLng(userLat, userLong)
                    if (this@MapActivity::pharmaciesList.isInitialized)
                        checkNearestPharm(it)
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
            10 -> requestUpdates()
            else -> return
        }
    }

    private fun requestUpdates() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
                ActivityCompat.requestPermissions(this,
                    arrayOf(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.INTERNET
                    )
                    , 10
                )
            return
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0f, listener)
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0f, listener)
        locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 5000, 0f, listener)
    }

    private fun initGoogleMaps(savedInstanceState: Bundle?) {
        // *** IMPORTANT ***
        // MapView requires that the Bundle you pass contain _ONLY_ MapView SDK
        // objects or sub-Bundles.
        var mapViewBundle: Bundle? = null
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY)
        }
        mapView.onCreate(mapViewBundle)

        mapView.getMapAsync(this)
    }

    private fun getLastKnowLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestUpdates()
        }
        fusedLocationProviderClient.lastLocation.addOnCompleteListener { task ->
            val location = task.result
            val name = intent.getStringExtra("NAME")
            latTV.text = location?.latitude.toString()
            longTV.text = location?.longitude.toString()
            location?.let {
                userLocation = it
            }
            mapView.getMapAsync {
                getPharmacies(it, location)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        var mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY)
        if (mapViewBundle == null) {
            mapViewBundle = Bundle()
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle)
        }

        mapView.onSaveInstanceState(mapViewBundle)
    }


    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onPause() {
        mapView.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        mapView.onDestroy()
        super.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

}