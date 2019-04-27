package com.martin.teami.fragments

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.martin.teami.R
import com.martin.teami.adapters.HistoryAdapter
import com.martin.teami.adapters.PerformanceAdapter
import com.martin.teami.models.History
import com.martin.teami.models.Performance
import com.martin.teami.models.PerformanceResponse
import com.martin.teami.retrofit.RepresentativesInterface
import com.martin.teami.utils.Consts
import com.martin.teami.utils.getID
import kotlinx.android.synthetic.main.fragment_history.*
import kotlinx.android.synthetic.main.fragment_performance.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class PerformanceFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        arguments?.let {
            getUserData(it.getString("token"), getID(context))
        }
        return inflater.inflate(R.layout.fragment_performance, container, false)
    }

    private fun getUserData(token: String, phoneId: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl(Consts.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(RepresentativesInterface::class.java)
            .getUserPerformance(token, phoneId).enqueue(object : Callback<PerformanceResponse> {
                override fun onFailure(call: Call<PerformanceResponse>, t: Throwable) {
                }

                override fun onResponse(call: Call<PerformanceResponse>, response: Response<PerformanceResponse>) {
                    val meResponse = response.body()
                    showUserInfo(meResponse?.performance)
                }
            })
    }

    private fun showUserInfo(performance: List<Performance>?) {
        performance?.let {
            val adapter = PerformanceAdapter(performance)
            performanceRV.layoutManager = LinearLayoutManager(context)
            performanceRV.adapter = adapter
        }
    }
}
