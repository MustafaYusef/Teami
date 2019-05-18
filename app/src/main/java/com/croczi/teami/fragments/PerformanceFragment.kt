package com.croczi.teami.fragments

import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.intrusoft.scatter.ChartData

import com.croczi.teami.R
import com.croczi.teami.adapters.PerformanceAdapter
import com.croczi.teami.models.Performance
import com.croczi.teami.models.PerformanceResponse
import com.croczi.teami.retrofit.RepresentativesInterface
import com.croczi.teami.utils.Consts
import com.croczi.teami.utils.getID
import kotlinx.android.synthetic.main.fragment_performance.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class PerformanceFragment : Fragment() {
    private lateinit var token: String
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_performance, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.getString("token")?.let {
            token = it
            getUserData(token, getID(context))
        }
        performanceSwipe?.setOnRefreshListener { getUserData(token, getID(context)) }
    }

    private fun getUserData(token: String, phoneId: String) {
        performanceSwipe?.isRefreshing = true
        val retrofit = Retrofit.Builder()
            .baseUrl(Consts.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(RepresentativesInterface::class.java)
            .getUserPerformance(token, phoneId).enqueue(object : Callback<PerformanceResponse> {
                override fun onFailure(call: Call<PerformanceResponse>, t: Throwable) {
                    performanceSwipe?.isRefreshing = false
                }

                override fun onResponse(call: Call<PerformanceResponse>, response: Response<PerformanceResponse>) {
                    performanceSwipe?.isRefreshing = false
                    if (response.body() != null) {
                        response.body()?.let {
                            val performanceResponse = it
                            if (performanceResponse.performance.isNotEmpty()) {
                                showUserInfo(performanceResponse.performance)
                                showTotal(performanceResponse.performance)
                            }
                        }
                    }
                }
            })
    }

    private fun showTotal(performance: List<Performance>?) {
        performance?.let {
            var total = 0f
            for (item in performance) {
                when (item.classX) {
                    "A" -> {
                        if (item.activationRatio.toFloat() < 1f)
                            total += item.activationRatio.toFloat() * 0.6f
                        else total += 0.6f
                    }
                    "B" -> {
                        if (item.activationRatio.toFloat() < 1f)
                            total += item.activationRatio.toFloat() * 0.3f
                        else total += 0.3f
                    }
                    "C" -> {
                        if (item.activationRatio.toFloat() < 1f)
                            total += item.activationRatio.toFloat() * 0.1f
                        else total += 0.1f
                    }
                }
            }
            total = (total + 0.01f)* 100
            Math.floor(total.toDouble())
            val totalInt = total.toInt()
            val data = arrayListOf<ChartData>()
            data.add(
                ChartData(
                    "$totalInt%",
                    totalInt.toFloat(),
                    Color.WHITE
                    , Color.parseColor("#871E20")
                )
            )
            data.add(ChartData("", 100f, Color.parseColor("#B5292C"), Color.parseColor("#B5292C")))
            poPie.setCenterCircleColor(Color.parseColor("#ff3B35"))
            poPie.setChartData(data)
            poPie.partitionWithPercent(true)
        }
    }

    private fun showUserInfo(performance: List<Performance>?) {
        performance?.let {
            val adapter = PerformanceAdapter(performance)
            performanceRV?.layoutManager = LinearLayoutManager(context)
            performanceRV?.adapter = adapter
        }
    }
}
