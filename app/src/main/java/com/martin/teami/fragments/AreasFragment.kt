package com.martin.teami.fragments

import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.martin.teami.R
import com.martin.teami.adapters.AreaAdapter
import com.martin.teami.models.MeRequest
import com.martin.teami.models.MeResponse
import com.martin.teami.models.User
import com.martin.teami.retrofit.RepresentativesInterface
import com.martin.teami.utils.Consts
import kotlinx.android.synthetic.main.fragment_areas.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class AreasFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        arguments?.let {
            getUserData(it.getString("token"), getID())
        }
        return inflater.inflate(R.layout.fragment_areas, container, false)
    }

    private fun getUserData(token: String, phoneId: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl(Consts.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(RepresentativesInterface::class.java)
            .getMe(MeRequest(token, phoneId)).enqueue(object : Callback<MeResponse> {
                override fun onFailure(call: Call<MeResponse>, t: Throwable) {
                }

                override fun onResponse(call: Call<MeResponse>, response: Response<MeResponse>) {
                    val meResponse = response.body()
                    showUserInfo(meResponse?.user)
                }
            })
    }

    private fun showUserInfo(user: User?) {
        user?.let {
            val adapter = AreaAdapter(user.Coverage_Area)
            areasRV.layoutManager = LinearLayoutManager(context)
            areasRV.adapter = adapter
        }
    }


    fun getID(): String {
        return Settings.Secure.getString(context?.contentResolver, Settings.Secure.ANDROID_ID)
    }

}
