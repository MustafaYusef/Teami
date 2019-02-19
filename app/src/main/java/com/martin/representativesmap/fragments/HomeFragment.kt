package com.martin.representativesmap.fragments


import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.*
import com.martin.representativesmap.R
import com.martin.representativesmap.activities.AddDoctor
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : Fragment() {



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addDoctorBtn.setOnClickListener {
            val intent= Intent(activity, AddDoctor::class.java)
            startActivity(intent)
        }
    }

}
