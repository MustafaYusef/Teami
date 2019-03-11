package com.martin.teami.adapters

import android.location.Location
import android.os.Build
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.martin.teami.R
import com.martin.teami.models.Pharmacy
import kotlinx.android.synthetic.main.pharmacy_item.view.*

class PharmaciesAdapter(val pharmacies: List<Pharmacy>, val location: Location?) :
    RecyclerView.Adapter<PharmaciesAdapter.PharmaciesViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PharmaciesViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.pharmacy_item, parent, false)
        return PharmaciesViewHolder(view)
    }

    override fun getItemCount() = pharmacies.size

    override fun onBindViewHolder(viewHolder: PharmaciesViewHolder, position: Int) {
        viewHolder.setPharmacy(pharmacies[position], location)
    }

    class PharmaciesViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

        fun setPharmacy(pharmacy: Pharmacy, location: Location?) {
            val pharmacyLocation = Location("pharmacy")
            pharmacyLocation.longitude = pharmacy.longitude.toDouble()
            pharmacyLocation.latitude = pharmacy.latitude.toDouble()
            var distance=0
            location?.let {
                distance = it.distanceTo(pharmacyLocation).toInt()
            }
            view.pharmacyNameTV.text = pharmacy.name

            val distanceString = if (distance >= 1000)
                "${distance / 1000} km"
            else "$distance m"
            view.distanceTV.text = distanceString
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) {
//                view.constLay.setBackgroundColor(
//                    view.resources.getColor(
//                        R.color.registered,
//                        view.resources.newTheme()
//                    )
//                )
            }
        }
    }
}