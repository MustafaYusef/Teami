package com.martin.teami.fragments


import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.*
import android.widget.Toast
import com.martin.teami.R
import com.martin.teami.activities.AddDoctor
import com.martin.teami.activities.FullDetailsActivity
import com.martin.teami.models.MeRequest
import com.martin.teami.models.MeResponse
import com.martin.teami.retrofit.RepresentativesInterface
import com.martin.teami.utils.Consts
import com.martin.teami.utils.Consts.BASE_URL
import com.martin.teami.utils.checkExpirationLimit
import com.orhanobut.hawk.Hawk
import kotlinx.android.synthetic.main.fragment_home.*
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*

class HomeFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


//        val sortedMarkers = (activity as MainActivity).checkNearestMarker(null)
        val token = arguments?.getString("TOKEN")
        val phoneId = arguments?.getString("PHONEID")
        val tokenExp = arguments?.getInt("EXP")
        val calendar: Calendar = Hawk.get(Consts.LOGIN_TIME)
        if (token != null && phoneId != null && tokenExp != null) {
            getUserData(token, phoneId)
            addDoctorBtn.setOnClickListener {
                checkExpirationLimit(token, tokenExp, phoneId, calendar, requireActivity())
                val intent = Intent(activity, AddDoctor::class.java)
                startActivity(intent)
            }
        }
    }

    fun setCardView(isNear: Boolean) {
        if (isNear) {
            imageView4.setImageResource(R.drawable.ic_my_location_green_24dp)
            detailsCV.setOnClickListener {
                val intent = Intent(requireContext(), FullDetailsActivity::class.java)
                startActivity(intent)
            }
        } else {
            imageView4.setImageResource(R.drawable.ic_my_location_red_24dp)
            detailsCV.setOnClickListener(null)
        }
    }

    private fun getUserData(token: String, phoneId: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val userCallback = retrofit.create(RepresentativesInterface::class.java)
            .getMe(MeRequest(token, phoneId)).enqueue(object : Callback<MeResponse> {
                override fun onFailure(call: Call<MeResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), t.message, Toast.LENGTH_LONG).show()
                }

                override fun onResponse(call: Call<MeResponse>, response: Response<MeResponse>) {
                    val meResponse = response.body()
                }
            })
    }
}
