package com.croczi.teami.fragments


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.croczi.teami.R
import com.croczi.teami.adapters.UserOrdersAdapter
import com.croczi.teami.models.UserOrderResponse
import com.croczi.teami.models.UserOrder
import com.croczi.teami.retrofit.RepresentativesInterface
import com.croczi.teami.utils.Consts.BASE_URL
import com.croczi.teami.utils.getID
import kotlinx.android.synthetic.main.fragment_user_orders.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class UserOrdersFragment : Fragment() {

    private lateinit var token: String
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_user_orders, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.getString("token")?.let {
            token = it
            getUserData(token, getID(context))
        }
        userOrderResfresh.setOnRefreshListener { getUserData(token, getID(context)) }
    }

    private fun getUserData(token: String, phoneId: String) {
        userOrderResfresh.isRefreshing = true
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(RepresentativesInterface::class.java)
            .checkUserOrders(token, phoneId).enqueue(object : Callback<UserOrderResponse> {
                override fun onFailure(call: Call<UserOrderResponse>, t: Throwable) {
                    userOrderResfresh.isRefreshing = false
                }

                override fun onResponse(call: Call<UserOrderResponse>, response: Response<UserOrderResponse>) {
                    userOrderResfresh.isRefreshing = false
                    if (response.body() != null) {
                        response.body()?.let {
                            val userOrdersResponse = it
                            if (userOrdersResponse.userOrders.isNotEmpty())
                                showUserInfo(userOrdersResponse.userOrders)
                        }
                    }
                }
            })
    }

    private fun showUserInfo(orders: List<UserOrder>?) {
        orders?.let {
            val adapter = UserOrdersAdapter(it)
            userOrdersRV.layoutManager = LinearLayoutManager(context)
            userOrdersRV.adapter = adapter
        }
    }

}