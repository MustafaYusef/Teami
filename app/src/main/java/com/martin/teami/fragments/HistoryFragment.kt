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
import com.martin.teami.adapters.AreaAdapter
import com.martin.teami.adapters.HistoryAdapter
import com.martin.teami.models.History
import com.martin.teami.models.MeRequest
import com.martin.teami.models.HistoryResponse
import com.martin.teami.models.User
import com.martin.teami.retrofit.RepresentativesInterface
import com.martin.teami.utils.Consts
import com.martin.teami.utils.getID
import kotlinx.android.synthetic.main.fragment_areas.*
import kotlinx.android.synthetic.main.fragment_history.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class HistoryFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        arguments?.let {
            getUserData(it.getString("token"), getID(context))
        }
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    private fun getUserData(token: String, phoneId: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl(Consts.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(RepresentativesInterface::class.java)
            .getHistory(token, phoneId).enqueue(object : Callback<HistoryResponse> {
                override fun onFailure(call: Call<HistoryResponse>, t: Throwable) {
                }

                override fun onResponse(call: Call<HistoryResponse>, response: Response<HistoryResponse>) {
                    val meResponse = response.body()
                    showUserInfo(meResponse?.history)
                }
            })
    }

    private fun showUserInfo(history: List<History>?) {
        history?.let {
            val adapter = HistoryAdapter(history)
            historyRV.layoutManager = LinearLayoutManager(context)
            historyRV.adapter = adapter
        }
    }

}
