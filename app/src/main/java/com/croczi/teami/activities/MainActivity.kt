package com.croczi.teami.activities

import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.multidex.MultiDex
import androidx.appcompat.app.AppCompatDelegate
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.croczi.teami.R
import com.croczi.teami.database.entitis.*
import com.croczi.teami.fragments.LoginFragment
import com.croczi.teami.fragments.MainFragment
import com.croczi.teami.models.*
import com.croczi.teami.retrofit.NetworkTools
import com.croczi.teami.utils.*
import com.croczi.teami.utils.Consts.AppLockedExtra
import com.croczi.teami.utils.Consts.LOGIN_RESPONSE_SHARED
import com.croczi.teami.utils.Consts.SHOULD_LOGOUT
import com.croczi.teami.utils.UserStatus.*
import com.facebook.drawee.backends.pipeline.Fresco
//import com.google.firebase.id.FirebaseInstanceId
import com.mustafayusef.holidaymaster.utils.corurtins
import com.mustafayusef.sharay.database.databaseApp
import com.orhanobut.hawk.Hawk
import kotlinx.android.synthetic.main.activity_full_details.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO


class MainActivity : AppCompatActivity(),ConnectivityReceiver.ConnectivityReceiverListener {
    var scope=CoroutineScope(IO)
    var isReg=false
    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        println("Connection state   :"+isConnected)
        if(isConnected){
            scope.launch {
                async {
                    var ListOrder=db!!.orders_Dao().getAllOrders()
                    if(!ListOrder.isEmpty()){
                        for(i in 0 until ListOrder.size){
                            postOrderOnline(ListOrder[i])
                        }
                        withContext(Dispatchers.Main){
                            Toast.makeText (applicationContext,getString(R.string.orderSends),Toast.LENGTH_LONG)
                                .show()
                        }


                    }
                }.await()
                async {
                    var ListFeed=db!!.feedBack_Dao().getAllResource()
                    if(!ListFeed.isEmpty()){
                        for(i in 0 until ListFeed.size){
                            postFeedOnline(ListFeed[i])
                        }
                        withContext(Dispatchers.Main){
                            Toast.makeText (applicationContext,getString(R.string.reportSends),Toast.LENGTH_LONG)
                                .show()
                        }
                    }
                }.await()
            }

        }
    }

   fun convertToStr(split: List<String>):List<String> {
       var ListInt= mutableListOf<String>()
       for(i in 0 until split.size){
           ListInt.add(split[i])
       }
       return ListInt
   }
    fun convertToInt(split: List<String>):List<Int> {
        var ListInt= mutableListOf<Int>()
        for(i in 0 until split.size){
            ListInt.add(split[i].toInt())
        }
        return ListInt
    }
    private fun postFeedOnline(feedbackRequestLocal: FeedbackRequestLocal) {
        val feedbackRequest = FeedbackRequest(
            feedbackRequestLocal.token,
            feedbackRequestLocal.phoneId,
            feedbackRequestLocal.resourceType,
            feedbackRequestLocal.resourceId,
            feedbackRequestLocal.statusId,
            feedbackRequestLocal.note,
            feedbackRequestLocal.activityType,
            feedbackRequestLocal.remindersProducts,
            feedbackRequestLocal.callProducts,feedbackRequestLocal.created_at
                )
        feedbackRequest?.let {
            NetworkTools.postFeedback(it, {
                CoroutineScope(IO).launch {
                  async {
                      println("Send Feed  ${feedbackRequestLocal.IdDb} " +
                              " delete"+db!!.feedBack_Dao().deleteFeedBackById(feedbackRequestLocal.IdDb))

                  }  .await()
                }
            }, { message ->
              //  scope.cancel(message)
        })
    }}
    private fun postOrderOnline(orderRequestLocal: OrderRequestLocal) {
        //val resource: MyResources = intent.getParcelableExtra("RESOURCE")
        val orderRequest = OrderRequest(
            orderRequestLocal.token,
            orderRequestLocal.phone_id,
            convertToInt(orderRequestLocal.item_idArray.split(',')) ,
            convertToStr (orderRequestLocal.quantityArray .split(',')),
            orderRequestLocal.resource_type,
            orderRequestLocal.resource_id,orderRequestLocal.created_at
        )
        NetworkTools.postOrder(orderRequest, {
            CoroutineScope(IO).launch {
                async {
                    println("Send Order ${orderRequestLocal.IdDb} " +
                            " delete"+db!!.orders_Dao().deleteOrderById(orderRequestLocal.IdDb))
                }  .await()
            }
        }, { message ->
            //scope.cancel()
        })
    }

    private var loginResponse: LoginResponse? = null
      var token:String?=null
    var db: databaseApp?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        setTheme(R.style.AppTheme_NoActionBar)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        MultiDex.install(this)
        Fresco.initialize(this)
        Hawk.init(this).build()

        db= this?.let { databaseApp(it) }

//        NetworkTools.checkIfAppIsLocked({ appLockedResponse ->
//            if (appLockedResponse.isLocked)
//                lockApp(appLockedResponse)
//            else
//                launchApp()
//        }, {
//            launchApp()
//        })
        launchApp()

        if(!isReg){
            try {
                registerReceiver(ConnectivityReceiver()
                    , IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
            }catch (e:Exception){

            }

            isReg=true
            ConnectivityReceiver.connectivityReceiverListener = this
        }


    }
     fun getStatusDoctor() {
        var list= mutableListOf<StatusResourceLocal>()
        token?.let {
            NetworkTools.getStatus(it, getID(this), { response ->
               // initFeedback(response.status)
               CoroutineScope(IO).launch {
                 async {
                     db!!.statusDoc_Dao().deleteAllStatusDoc()
                 }.await()

               async {
                   for(i in 0 until  response.status.size){
                       var Status=StatusResourceLocal(id=response.status[i].id,
                           text=response.status[i].text)
                       list.add(Status)
                   }
               }.await()


                  async {
                      db!!.statusDoc_Dao().inserAllStatusDoc(list)
                  }  .await()
                }
            }, { message ->

                fbBtnProgressBar?.visibility = View.GONE
                feedbackBtn?.visibility = View.VISIBLE
                Toast.makeText(
                    this,
                    message,
                    Toast.LENGTH_LONG
                )
                    .show()
            })
        } ?: run { checkUser(this) { _, _ -> } }
    }
     fun getStatusPhar() {
        var list= mutableListOf<StatusPharmasyResourceLocal>()
        token?.let {
            NetworkTools.getPharmacyStatus(it, getID(this), { response ->
                CoroutineScope(IO).launch {
                    async {
                        db!!.statusPh_Dao().deleteAllStatusPharnasy()
                    }.await()

                async {
                    for(i in 0 until  response.PharmacyStatus .size){
                        var Status=StatusPharmasyResourceLocal(id=response.PharmacyStatus[i].id,
                            text=response.PharmacyStatus[i].text)
                        list.add(Status)
                    }
                } .await()
                   async {
                       db!!.statusPh_Dao().inserAllStatusPharnasy(list)
                   }.await()
                }


                // initFeedback(response.status)
            }, { message ->
                fbBtnProgressBar?.visibility = View.GONE
                feedbackBtn?.visibility = View.VISIBLE
                Toast.makeText(
                    this,
                    message,
                    Toast.LENGTH_LONG
                )
                    .show()
            })
        } ?: run { checkUser(this) { _, _ -> } }
    }

     fun getItems() {
        var list= mutableListOf<ItemLocal>()
        token?.let {
            NetworkTools.getItems(it, getID(this), { response ->
                CoroutineScope(IO).launch {
                    async {
                        db!!.items_Dao().deleteAllItems()
                    }.await()


             async {
                 for(i in 0 until  response.items.size){
                     var Status=ItemLocal(id=response.items[i].id,
                         name=response.items[i].name,
                         description=response.items[i].description,
                         companyName=response.items[i].companyName)
                     list.add(Status)
                 }
             }.await()
                    async {
                        db!!.items_Dao().inserAllItems(list)
                    }.await()
                }
                // initFeedback(response.status)
            }, { message ->
                fbBtnProgressBar?.visibility = View.GONE
                feedbackBtn?.visibility = View.VISIBLE
                Toast.makeText(
                    this,
                    message,
                    Toast.LENGTH_LONG
                )
                    .show()
            })
        } ?: run { checkUser(this) { _, _ -> } }
    }
    suspend fun setDataBase(){
        CoroutineScope(IO).launch {
            async {
                getStatusDoctor()

            }.await()
            async {
                getStatusPhar()
            }.await()
            async {
                getItems()
            }.await()
        }
    }
    private fun lockApp(appLockedResponse: AppLockedResponse) {
        val intent = Intent(this, AppLockedActivity::class.java)
        intent.putExtra(AppLockedExtra, appLockedResponse)
        startActivity(intent)
    }

    private fun launchApp() {
       // Hawk.init(this).build()
//        FirebaseInstanceId.getInstance().
//            instanceId.addOnSuccessListener { instanceIdResult ->
//            val newToken = instanceIdResult.token
//            Log.d("Token", newToken)
//        }
        if (intent.getBooleanExtra(SHOULD_LOGOUT, false))
            gotoLogin()
        else
            checkUser(this) { status, loginResponse ->
                when (status) {
                    LoggedOut -> gotoLogin()
                    NetworkError ->{
                        corurtins.main {
                            // setDataBase()
                            gotoMain()
                        } //TODO: Make an actual network error page/fragment
                    }
                    LoggedIn -> {
                        this.loginResponse = loginResponse
                        token=loginResponse!!.token
                        CoroutineScope(IO).launch {
                            async{
                                setDataBase()
                            } .await()
                            async {
                                gotoMain()
                            }.await()
                        }
                    }
                }
            }




    }

    private  fun gotoLogin() {
        Hawk.deleteAll()
        val loginFragment = LoginFragment()
        supportFragmentManager.fragments.clear()
        supportFragmentManager.beginTransaction().add(R.id.fragLayout, loginFragment)
            .commitAllowingStateLoss()
    }

   suspend fun gotoMain() {
        val args = Bundle()
        args.putParcelable(LOGIN_RESPONSE_SHARED, Hawk.get(LOGIN_RESPONSE_SHARED))
        if (supportFragmentManager.fragments.isEmpty() || supportFragmentManager.fragments.last() !is MainFragment) {
            val mainFragment = MainFragment()
            mainFragment.arguments = args
            supportFragmentManager?.fragments?.clear()
            supportFragmentManager?.beginTransaction()?.add(R.id.fragLayout, mainFragment)
                ?.commitAllowingStateLoss()
        }
    }

//    override fun onStart() {
//        super.onStart()
//
//
//    }

    override fun onPause() {
        super.onPause()

    }
    override fun onStop() {
        super.onStop()

    }
    override fun onDestroy() {
        if(isReg){
            try {
                unregisterReceiver(ConnectivityReceiver())
            }catch (e:Exception){
             println("                "+e.message)
            }

            isReg=false
        }
        super.onDestroy()


    }

}
