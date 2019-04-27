package com.martin.teami.fragments


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import com.martin.teami.R

import com.martin.teami.models.MeResponse
import com.martin.teami.models.User
import kotlinx.android.synthetic.main.fragment_info.*

class InfoFragment : Fragment() {

    private var lastPosition = -1
    private val fadeDuration: Long = 500
    private lateinit var meResponse: MeResponse

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            meResponse=it.getParcelable("me")
            showUserInfo(meResponse.user)
            setAnimation(cardView4,0)
        }
    }

    private fun showUserInfo(user: User?) {
        cardView4.layoutParams
        user?.let {
            phone.text = user.Phone
            email.text = user.Email
            type.text = user.Role
            sup.text = user.Reporting_to
        }
    }

    fun setAnimation(viewToAnimate: View, position: Int) {
        if (position > lastPosition) {
            val anim = ScaleAnimation(
                0.8f,
                1.0f,
                0.8f,
                1.0f,
                Animation.RELATIVE_TO_SELF,
                0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f
            )
            anim.duration = fadeDuration
            viewToAnimate.startAnimation(anim)
            lastPosition = position
        }
    }
}
