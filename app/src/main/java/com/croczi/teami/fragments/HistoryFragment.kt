package com.croczi.teami.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.croczi.teami.R
import com.croczi.teami.adapters.HistoryAdapter
import com.croczi.teami.models.History
import com.croczi.teami.models.HistoryResponse
import com.croczi.teami.retrofit.RepresentativesInterface
import com.croczi.teami.utils.Consts
import com.croczi.teami.utils.getID
import kotlinx.android.synthetic.main.fragment_history.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class HistoryFragment : Fragment() {

    private lateinit var token: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.getString("token")?.let {
            token = it
            getUserData(token, getID(context))
        }
        historySwipe.setOnRefreshListener {
            getUserData(token, getID(context))
        }
    }

    private fun getUserData(token: String, phoneId: String) {
        historySwipe.isRefreshing=true
        val retrofit = Retrofit.Builder()
            .baseUrl(Consts.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(RepresentativesInterface::class.java)
            .getHistory(token, phoneId).enqueue(object : Callback<HistoryResponse> {
                override fun onFailure(call: Call<HistoryResponse>, t: Throwable) {
                    historySwipe.isRefreshing=false
                }

                override fun onResponse(call: Call<HistoryResponse>, response: Response<HistoryResponse>) {
                    historySwipe?.isRefreshing=false
                    if (response.body() != null) {
                        response.body()?.let {
                            val historyResponse = it
                            if (historyResponse.history.isNotEmpty())
                                showUserInfo(historyResponse.history)
                        }
                    }
                }
            })
    }

    private fun showUserInfo(history: List<History>?) {
        history?.let {
            val adapter = HistoryAdapter(history)
            historyRV?.layoutManager = LinearLayoutManager(context)
            historyRV?.adapter = adapter
        }
    }

}
