package com.croczi.teami.fragments


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.LocaleList
import androidx.annotation.RequiresApi
import com.google.android.material.chip.Chip
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.getbase.floatingactionbutton.FloatingActionsMenu

import com.croczi.teami.R
import com.croczi.teami.activities.AddDoctor
import com.croczi.teami.activities.AddPharmacy
import com.croczi.teami.activities.MainActivity
import com.croczi.teami.activities.ProfileActivity
import com.croczi.teami.adapters.ResourcesAdapter
import com.croczi.teami.models.*
import com.croczi.teami.retrofit.NetworkTools
import com.croczi.teami.retrofit.RepresentativesInterface
import com.croczi.teami.retrofit.ToolsInterface
import com.croczi.teami.retrofit.addTLSSupport
import com.croczi.teami.utils.*
import com.croczi.teami.utils.Consts.BASE_URL
import com.croczi.teami.utils.Consts.LOGIN_RESPONSE_SHARED
import com.croczi.teami.utils.Consts.USER_LOCATION
import com.mustafayusef.holidaymaster.utils.corurtins
import com.mustafayusef.sharay.database.databaseApp
import com.mustafayusef.sharay.database.entitis.MyResourcesLocal
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.android.synthetic.main.search_layout.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import kotlin.Comparator

class MainFragment : androidx.fragment.app.Fragment() {

    private var checkedSearchChip: Int = 0
    private var searchText: String = ""
    private var meResponse: MeResponse? = null
    var resourcesList: List<MyResources>? = null
    var resourcesListMutble= mutableListOf<MyResources>()
    private var userLocation: Location? = null
    private var token: String? = null
    private var tokenExp: Long? = 0
    lateinit var adapter: ResourcesAdapter
    private lateinit var locationUtils: LocationUtils
    private var permissionCount = 0
    private lateinit var myContext: Context
    private var loginResponse: LoginResponse? = null

    var  localResource= mutableListOf<MyResourcesLocal>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

        return inflater.inflate(R.layout.fragment_main, container, false)
    }
    var db:databaseApp?=null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        super.onViewCreated(view, savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            frameLayout?.elevation = 16f
            frameLayout?.translationZ = 16f
        }

         db= context?.let { databaseApp(it) }
        loginResponse = arguments?.get(LOGIN_RESPONSE_SHARED) as LoginResponse?
        token = loginResponse?.token
        tokenExp = loginResponse?.expire

//        providerDisabledLayout?.visibility = View.VISIBLE
//        mockLayout?.visibility = View.GONE
//        resourcesRV?.visibility = View.GONE
//        resourcesRefresh?.visibility = View.GONE


        pink_icon?.visibility = View.GONE
        context?.let {
            myContext = it
        }

//        resourcesRV?.itemAnimator?.changeDuration=0
//        if (resourcesRV?.itemAnimator is SimpleItemAnimator)
//            (resourcesRV?.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        locationUtils = LocationUtils.getInstance(this, myContext)
        userLocation = locationUtils.userLocation
        enable_gps_button?.setOnClickListener {
            LocationUtils.initGPS(myContext)
        }
        profileIV?.setOnClickListener {
            val i = Intent(context, ProfileActivity::class.java)
            startActivity(i)
        }
        adapter = ResourcesAdapter(resourcesList, userLocation, myContext)
        adapter.setHasStableIds(true)
        resourcesRefresh?.setOnRefreshListener {
            getMyResources()
            emptyListLayout?.visibility = View.INVISIBLE
            errorLayout?.visibility = View.INVISIBLE

        }
        getUserData(token, getID(context))
        getMyResources()

        adapter.notifyDataSetChanged()
        setSearchLayoutListener()
    }

    private fun getUserData(token: String?, phoneId: String) {
        checkLocation()
        NetworkTools.getUserInfo(MeRequest(token, phoneId), {
            meResponse = it
            setFabs(meResponse)
        }, {
            Toast.makeText(context, "Error getting user info", Toast.LENGTH_LONG).show()
        })
    }

    private fun setFabs(meResponse: MeResponse?) {
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
                intent.putExtra(USER_LOCATION, userLocation)
                startActivity(intent)
            } else Toast.makeText(
                context,
                getString(R.string.location_unavailable),
                Toast.LENGTH_LONG
            ).show()
        }
        if (meResponse?.user?.Role != "sales_delegate") {
            addDocFab?.setOnClickListener {
                userLocation = locationUtils.userLocation
                if (userLocation != null) {
                    val intent = Intent(context, AddDoctor::class.java)
                    startActivity(intent)
                } else Toast.makeText(
                    context,
                    getString(R.string.location_unavailable),
                    Toast.LENGTH_LONG
                ).show()
            }
        } else addDocFab?.visibility = View.GONE
    }

    private fun setSearchLayoutListener() {
        searchIV?.setOnClickListener {
            if (search_layout.visibility == View.GONE) {
                search_layout.visibility = View.VISIBLE
            } else search_layout.visibility = View.GONE
        }
        setSelectedChip(nameChip)
        nameChip?.setOnClickListener {
            setSelectedChip(nameChip)
            filterAndSortResources()
        }
        areaChip?.setOnClickListener {
            setSelectedChip(areaChip)
            filterAndSortResources()
        }
        streetChip?.setOnClickListener {
            setSelectedChip(streetChip)
            filterAndSortResources()

        }
        provinceChip?.setOnClickListener {
            setSelectedChip(provinceChip)
            filterAndSortResources()

        }
        searchView?.afterTextChanged {
            searchText = it
            filterAndSortResources()
        }
    }

    private fun setSelectedChip(selectedChip: Chip) {
        setChipColors(nameChip, selectedChip == nameChip)
        setChipColors(streetChip, selectedChip == streetChip)
        setChipColors(areaChip, selectedChip == areaChip)
        setChipColors(provinceChip, selectedChip == provinceChip)
        checkedSearchChip = selectedChip.id
    }

    private fun setChipColors(chip: Chip, selected: Boolean) {
        if (selected) {
            chip.setTextColor(Color.parseColor("#E0E0E0"))
            chip.setChipBackgroundColorResource(R.color.colorPrimary)
        } else {
            chip.setTextColor(Color.parseColor("#666666"))
            chip.setChipBackgroundColorResource(R.color.chipBackgroundColor)
        }
    }


    fun filterAndSortResources(): List<MyResources>? {
        checkLocation()
        val sortedDoctors = resourcesList
        val filtered = arrayListOf<MyResources>()
        userLocation = locationUtils.userLocation
        if (searchText.isBlank() || searchText.isEmpty()) {
            filtered.addAll(sortResourcesByDistance())
        } else {
            filtered.addAll(filterResourcesByQuery())
        }
        userLocation = locationUtils.userLocation
        adapter.userLocation = userLocation!!
        if (filtered.isNotEmpty()) {
            resourcesRV?.visibility = View.VISIBLE
            emptyListLayout?.visibility = View.INVISIBLE
            adapter.allResources = filtered
        }
        resourcesList = sortedDoctors
        if (!sortedDoctors.isNullOrEmpty())
            adapter.notifyDataSetChanged()
        return sortedDoctors
    }

    private fun sortResourcesByDistance(): List<MyResources> {
        val sortedResources = arrayListOf<MyResources>()
        resourcesList?.let {
            sortedResources.addAll(it)
        }
        userLocation?.let { userLocation ->
            Collections.sort(sortedResources, Comparator<MyResources> { marker1, marker2 ->
                val locationA = Location("point A")
                locationA.latitude = marker1.latitude.toDouble()
                locationA.longitude = marker1.longitude.toDouble()
                val locationB = Location("point B")
                locationB.latitude = marker2.latitude.toDouble()
                locationB.longitude = marker2.longitude.toDouble()
                val distanceOne = userLocation.distanceTo(locationA)
                val distanceTwo = userLocation.distanceTo(locationB)
                return@Comparator distanceOne.compareTo(distanceTwo)
            })
        }
        return sortedResources
    }

    private fun filterResourcesByQuery(): List<MyResources> {
        val sortedResources = arrayListOf<MyResources>()
        resourcesList?.let { resourcesList ->
            sortedResources.addAll(when (checkedSearchChip) {
                R.id.nameChip -> {
                    resourcesList.filter {
                        it.name.toLowerCase().contains(searchText)
                    }
                }
                R.id.areaChip -> {
                    resourcesList.filter {
                        it.reign.toLowerCase().contains(searchText)
                    }
                }
                R.id.streetChip -> {
                    resourcesList.filter {
                        it.street.toLowerCase().contains(searchText)
                    }
                }
                R.id.provinceChip -> {
                    resourcesList.filter {
                        it.organisation.toLowerCase().contains(searchText)
                    }
                }
                else -> resourcesList
            })
        }
        return sortedResources
    }


    private fun checkLocation() {
        userLocation?.let {
            if (it.isFromMockProvider) {
                mockLayout?.visibility = View.VISIBLE
                providerDisabledLayout?.visibility = View.GONE
                resourcesRV?.visibility = View.GONE
                resourcesRefresh?.visibility = View.GONE
                sendMockReport()
            } else {
                providerDisabledLayout?.visibility = View.GONE
                mockLayout?.visibility = View.GONE
                resourcesRV?.visibility = View.VISIBLE
                resourcesRefresh?.visibility = View.VISIBLE
            }
        }
        if (locationUtils.providerDisabled) {
            providerDisabledLayout?.visibility = View.VISIBLE
            mockLayout?.visibility = View.GONE
            resourcesRV?.visibility = View.GONE
            resourcesRefresh?.visibility = View.GONE
        }
    }

    private fun sendMockReport() {
        meResponse?.let { meResponse2 ->
            val mockUser = MockUser(
                meResponse2.user.UserName,
                meResponse2.user.Email,
                meResponse2.user.Phone,
                meResponse2.user.Role,
                meResponse2.user.Reporting_to,
                meResponse2.user.Userid
            )
            val retrofit = Retrofit.Builder()
                .baseUrl("https://hidden-wave-32027.herokuapp.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            retrofit.create(ToolsInterface::class.java).postMockUser(mockUser)
                .enqueue(object : Callback<MockResponse> {
                    override fun onFailure(call: Call<MockResponse>, t: Throwable) {

                    }

                    override fun onResponse(
                        call: Call<MockResponse>,
                        response: Response<MockResponse>
                    ) {
                        Toast.makeText(myContext, "Reported to Supervisor", Toast.LENGTH_LONG)
                            .show()
                    }
                })
        }
    }

    fun getMyResources() {
        localResource.clear()
        checkLocation()
        resourcesRefresh?.isRefreshing = true
        token?.let {
            NetworkTools.getMyResources(it, getID(requireContext()), {
                resourcesRefresh?.isRefreshing = false
                errorLayout?.visibility = View.INVISIBLE
                resourcesRV?.visibility = View.VISIBLE
                resourcesList = it.Resource
                userLocation = locationUtils.userLocation
                if (userLocation != null)
                    filterAndSortResources()
                resourcesRV?.adapter = adapter
                resourcesRV?.layoutManager =
                    androidx.recyclerview.widget.LinearLayoutManager(context)
                adapter.notifyDataSetChanged()
                 for(i in 0 until resourcesList!!.size) {
                     var myLocal=MyResourcesLocal(resourceType = resourcesList!![i].resourceType,
                         id= resourcesList!![i].id, name=resourcesList!![i].name,
                      street=resourcesList!![i].street, workTime=resourcesList!![i].workTime ,
                         organisation=resourcesList!![i].organisation,
                      speciality=resourcesList!![i].speciality,
                      hospital=resourcesList!![i].hospital,
                              reign=resourcesList!![i].reign,
                         latitude= resourcesList!![i].latitude,
                      longitude=resourcesList!![i].longitude)
                     localResource.add(myLocal)
                 }

              //  println("Locallllllllllllllllllllllll;; "+localResource[0].hospital)
                  CoroutineScope(IO).launch{
                      async {
                          db!!.resource_Dao().deleteAllResource()
                      }.await()
                      async {
                          db!!.resource_Dao().inserAllResource(localResource.toList())
                      }.await()

                }
                //set the cash
            }, {
                resourcesListMutble.clear()

                resourcesRefresh?.isRefreshing = false
//                errorLayout?.visibility = View.VISIBLE
//                resourcesRV?.visibility = View.INVISIBLE

                errorLayout?.visibility = View.INVISIBLE
                resourcesRV?.visibility = View.VISIBLE

                CoroutineScope(IO).launch{
                   async {
                       var localRes=db!!.resource_Dao().getAllResource()
                       withContext(Dispatchers.Main){
                           for(i in 0 until localRes.size){
                               var myRes=MyResources( resourceType=localRes[i].resourceType,
                                   id=localRes[i].id,
                                   name=localRes[i].name,
                                   street=localRes[i].street,
                                   workTime=localRes[i].workTime,
                                   organisation=localRes[i].organisation,
                                   speciality=localRes[i].speciality,
                                   hospital=localRes[i].hospital,
                                   reign=localRes[i].reign,
                                   latitude=localRes[i].latitude,
                                   longitude=localRes[i].longitude)
                               resourcesListMutble.add(myRes)
                           }
                           resourcesList =resourcesListMutble
                           userLocation = locationUtils.userLocation
                           if (userLocation != null)
                               filterAndSortResources()
                           resourcesRV?.adapter = adapter
                           resourcesRV?.layoutManager =
                               androidx.recyclerview.widget.LinearLayoutManager(context)
                           adapter.notifyDataSetChanged()
                       }
                   }.await()



                }
                // desplay them
            })
        } ?: run { checkUser(requireActivity(), { _, _ -> }) }

    }
  suspend fun print1(localResource:MutableList<MyResourcesLocal>){
      withContext(Dispatchers.Main){

      }

  }
    @SuppressLint("WrongConstant")
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
                        else locationUtils.requestUpdates(myContext, false)
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
                    userLocation = locationUtils.userLocation
                    getMyResources()
                } else {
                    if (permissionCount > 1) {
                        if (!ActivityCompat.shouldShowRequestPermissionRationale(
                                myContext as Activity,
                                Manifest.permission.WRITE_CONTACTS
                            )
                        ) {
                            showMessageOKCancel(context as Activity,
                                getString(R.string.permissionsTitle),
                                getString(R.string.permissionMessage),
                                DialogInterface.OnClickListener { dialog, which ->
                                    locationUtils.requestUpdates(myContext, false)
                                },
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
        checkLocation()
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
