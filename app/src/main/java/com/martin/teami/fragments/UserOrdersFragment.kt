package com.martin.teami.fragments


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.martin.teami.R
import com.martin.teami.adapters.AreaAdapter
import com.martin.teami.adapters.UserOrdersAdapter
import com.martin.teami.models.MeRequest
import com.martin.teami.models.UserOrderResponse
import com.martin.teami.models.UserOrder
import com.martin.teami.retrofit.RepresentativesInterface
import com.martin.teami.utils.Consts
import com.martin.teami.utils.Consts.BASE_URL
import com.martin.teami.utils.getID
import kotlinx.android.synthetic.main.fragment_user_orders.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class UserOrdersFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        arguments?.let {
            getUserData(it.getString("token"), getID(context))
        }
        return inflater.inflate(R.layout.fragment_user_orders, container, false)
    }

    private fun getUserData(token: String, phoneId: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(RepresentativesInterface::class.java)
            .checkUserOrders(token, phoneId).enqueue(object : Callback<UserOrderResponse> {
                override fun onFailure(call: Call<UserOrderResponse>, t: Throwable) {
                }

                override fun onResponse(call: Call<UserOrderResponse>, response: Response<UserOrderResponse>) {
                    val userOrdersResponse = response.body()
                    showUserInfo(userOrdersResponse?.userOrders)
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
