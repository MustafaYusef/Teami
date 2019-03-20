package com.martin.teami.adapters

import android.content.Intent
import android.location.Location
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.martin.teami.R
import com.martin.teami.activities.FullDetailsActivity
import com.martin.teami.models.MyDoctor
import kotlinx.android.synthetic.main.doctor_item.view.*

class DoctorsAdapter(var doctors: List<MyDoctor>?, var userLocation: Location?) :
    RecyclerView.Adapter<DoctorsAdapter.DoctorsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DoctorsViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.doctor_item, parent, false)
        return DoctorsViewHolder(view)
    }

    override fun getItemCount() = doctors?.size?:0

    override fun onBindViewHolder(viewHolder: DoctorsViewHolder, position: Int) {
        viewHolder.setArea(doctors?.get(position))
    }

    inner class DoctorsViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

        fun checkIfNearMarker(doctor: MyDoctor): Boolean {
            val nearestDoc = Location("Nearest Doctor")
            nearestDoc.latitude = doctor.latitude.toDouble()
            nearestDoc.longitude = doctor.longitude.toDouble()

            userLocation?.let {
                val distance = it.distanceTo(nearestDoc)
                return distance < 1000
            }
            return false
        }

        fun setArea(doctor: MyDoctor?) {
            doctor?.let {
                view.doctorNameTV.text = it.name
                view.doctorAddressTV.text = it.reign.name
                view.doctorStreetTV.text = it.street
                view.doctorHospitalTV.text = it.hospital.name
                setCardView(doctor)
            }
        }

        fun setCardView(doctor: MyDoctor) {
            view.imageView7.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
            if (userLocation != null) {
                if (checkIfNearMarker(doctor)) {
                    view.imageView4?.setImageResource(R.drawable.ic_my_location_green_24dp)
                    view.detailsCV?.setOnClickListener {
                        val intent = Intent(view.context, FullDetailsActivity::class.java)
                        intent.putExtra("DOCTOR",doctor)
                        view.context.startActivity(intent)
                    }
                } else {
                    view.imageView4?.setImageResource(R.drawable.ic_my_location_red_24dp)
                    view.detailsCV?.setOnClickListener(null)
                }
            } else view.imageView4?.setImageResource(R.drawable.ic_not_available)
        }
    }

}