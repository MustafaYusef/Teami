package com.croczi.teami.fragments

import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.croczi.teami.R
import com.croczi.teami.adapters.AreaAdapter
import com.croczi.teami.models.MeResponse
import com.croczi.teami.models.User
import kotlinx.android.synthetic.main.fragment_areas.*


class AreasFragment : Fragment() {

    private lateinit var meResponse: MeResponse

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_areas, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.getParcelable<MeResponse>("me")?.let { it ->
            meResponse = it
            showUserInfo(meResponse.user)
        }
    }

    private fun showUserInfo(user: User?) {
        user?.let {
            val adapter = AreaAdapter(user.Coverage_Area)
            areasRV?.layoutManager = LinearLayoutManager(context)
            areasRV?.adapter = adapter
        }
    }


    fun getID(): String {
        return Settings.Secure.getString(context?.contentResolver, Settings.Secure.ANDROID_ID)
    }

}
