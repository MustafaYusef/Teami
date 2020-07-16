package com.croczi.teami.fragments


import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatDelegate
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast

import com.croczi.teami.R
import com.croczi.teami.activities.ForgotPasswordActivity
import com.croczi.teami.activities.MainActivity
import com.croczi.teami.database.entitis.ItemLocal
import com.croczi.teami.database.entitis.StatusPharmasyResourceLocal
import com.croczi.teami.database.entitis.StatusResourceLocal
import com.croczi.teami.models.ErrorResponse
import com.croczi.teami.models.ErrorResponseArray
import com.croczi.teami.models.LoginRequest
import com.croczi.teami.models.LoginResponse
import com.croczi.teami.retrofit.NetworkTools
import com.croczi.teami.retrofit.RepresentativesInterface
import com.croczi.teami.retrofit.addTLSSupport
import com.croczi.teami.utils.Consts
import com.croczi.teami.utils.Consts.BASE_URL
import com.croczi.teami.utils.Consts.LOGIN_RESPONSE_SHARED
import com.croczi.teami.utils.Consts.LOGIN_TIME
import com.croczi.teami.utils.checkUser
import com.mustafayusef.holidaymaster.utils.corurtins
import com.mustafayusef.sharay.database.databaseApp
import com.orhanobut.hawk.Hawk
import com.wajahatkarim3.easyvalidation.core.view_ktx.validEmail
import kotlinx.android.synthetic.main.activity_full_details.*
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import java.util.Collections.singletonList
import okhttp3.OkHttpClient
import okhttp3.CipherSuite
import okhttp3.TlsVersion
import okhttp3.ConnectionSpec
import okhttp3.internal.Version


class LoginFragment : androidx.fragment.app.Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        return inflater.inflate(R.layout.fragment_login, container, false)
    }
    var db: databaseApp?=null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        super.onViewCreated(view, savedInstanceState)
        loginBtn?.setOnClickListener {
            if (setValidation())
                initLogin()
        }
        db= context?.let { databaseApp(it) }
        forgotPasswordTV?.setOnClickListener {
            val intent = Intent(context, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }
        Hawk.init(context).build()
    }

    private fun setValidation(): Boolean {
        return when {
            emailET?.text.isNullOrBlank() && emailET?.text?.isEmpty()?:true -> {
                Toast.makeText(context, getString(R.string.email_empty), Toast.LENGTH_LONG).show()
                false
            }
            !(emailET?.validEmail()?:false) -> {
                Toast.makeText(context, getString(R.string.email_not_valid), Toast.LENGTH_LONG).show()
                false
            }
            passwordET?.text.isNullOrBlank() && passwordET?.text?.isEmpty()?:true -> {
                Toast.makeText(context, getString(R.string.password_empty), Toast.LENGTH_LONG).show()
                false
            }
            else -> true
        }
    }

    private fun initLogin() {
        var retrofitBuilder = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            retrofitBuilder.addTLSSupport()
        }
        val retrofit = retrofitBuilder.build()
        val loginInterface = retrofit.create(RepresentativesInterface::class.java)
        loginProgressBar?.visibility = View.VISIBLE
        loginBtn?.visibility = View.GONE
        val loginRequest = LoginRequest(emailET?.text.toString(), passwordET?.text.toString(), getID())
        val loginResponseCall = loginInterface.getToken(loginRequest)
        loginResponseCall.enqueue(object : Callback<LoginResponse> {
            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                loginProgressBar?.visibility = View.GONE
                loginBtn?.visibility = View.VISIBLE
                Toast.makeText(context, "Problem with the network", Toast.LENGTH_LONG).show()
            }

            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                loginProgressBar?.visibility = View.GONE
                loginBtn?.visibility = View.VISIBLE
                val loginResponse = response.body()
                if (!loginResponse?.token.isNullOrEmpty()) {
                    Hawk.put(LOGIN_RESPONSE_SHARED, loginResponse)
                    Hawk.put(LOGIN_TIME, Calendar.getInstance(TimeZone.getDefault()))
                    var token=Hawk.get<LoginResponse>(LOGIN_RESPONSE_SHARED).token
                    CoroutineScope(Dispatchers.IO).launch {
                        async{
                          setDataBase(token)
                        } .await()
                        async {
                            (requireActivity() as MainActivity).gotoMain()
                        }.await()
                    }





                } else if (response.code() == 406) {
                    val converter = retrofit.responseBodyConverter<ErrorResponse>(
                        ErrorResponse::class.java,
                        arrayOfNulls<Annotation>(0)
                    )
                    response.errorBody()?.let { errorBody ->
                        val errors = converter.convert(errorBody)
                        Toast.makeText(requireContext(), errors?.error, Toast.LENGTH_SHORT).show()
                    }
                } else if (response.code() == 400 || response.code() == 422) {
                    val converter = retrofit.responseBodyConverter<ErrorResponseArray>(
                        ErrorResponseArray::class.java,
                        arrayOfNulls<Annotation>(0)
                    )
                    response.errorBody()?.let { errorBody ->
                        val errors = converter.convert(errorBody)
                        Toast.makeText(requireContext(), errors?.error?.get(0), Toast.LENGTH_SHORT).show()
                    }
                }else if(response.code() == 401){
                    Toast.makeText(requireContext(),getString(R.string.invalidEmailOr) , Toast.LENGTH_LONG).show()
                }
                else{


                    response.errorBody()?.let { errorBody ->

                        Toast.makeText(requireContext(), errorBody?.string(), Toast.LENGTH_SHORT).show()
                        println( response)
                    }

                }
            }

        })

    }

    @SuppressLint("HardwareIds")
    fun getID(): String {
        return Settings.Secure.getString(context?.contentResolver, Settings.Secure.ANDROID_ID)
    }
    fun getStatusDoctor(token:String) {
        var list= mutableListOf<StatusResourceLocal>()
        token?.let {
            NetworkTools.getStatus(it, com.croczi.teami.utils.getID(context), { response ->
                // initFeedback(response.status)
                CoroutineScope(Dispatchers.IO).launch {
                    async {
                        db!!.statusDoc_Dao().deleteAllStatusDoc()
                    }.await()

                    async {
                        for(i in 0 until  response.status.size){
                            var Status= StatusResourceLocal(id=response.status[i].id,
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
                    context,
                    message,
                    Toast.LENGTH_LONG
                )
                    .show()
            })
        }
    }
    fun getStatusPhar(token:String) {
        var list= mutableListOf<StatusPharmasyResourceLocal>()
        token?.let {
            NetworkTools.getPharmacyStatus(it, com.croczi.teami.utils.getID(context), { response ->
                CoroutineScope(Dispatchers.IO).launch {
                    async {
                        db!!.statusPh_Dao().deleteAllStatusPharnasy()
                    }.await()

                    async {
                        for(i in 0 until  response.PharmacyStatus .size){
                            var Status= StatusPharmasyResourceLocal(id=response.PharmacyStatus[i].id,
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
                    context,
                    message,
                    Toast.LENGTH_LONG
                )
                    .show()
            })
        }
    }

    fun getItems(token:String) {
        var list= mutableListOf<ItemLocal>()
        token?.let {
            NetworkTools.getItems(it, com.croczi.teami.utils.getID(context), { response ->
                CoroutineScope(Dispatchers.IO).launch {
                    async {
                        db!!.items_Dao().deleteAllItems()
                    }.await()


                    async {
                        for(i in 0 until  response.items.size){
                            var Status= ItemLocal(id=response.items[i].id,
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
                    context,
                    message,
                    Toast.LENGTH_LONG
                )
                    .show()
            })
        }
    }
    suspend fun setDataBase(token:String){
        CoroutineScope(Dispatchers.IO).launch {
            async {
                getStatusDoctor(token)

            }.await()
            async {
                getStatusPhar(token)
            }.await()
            async {
                getItems(token)
            }.await()
        }
    }
}
