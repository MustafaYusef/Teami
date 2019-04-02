package com.martin.teami.adapters

import android.content.Intent
import android.location.Location
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.martin.teami.R
import com.martin.teami.activities.FullDetailsActivity
import com.martin.teami.models.MyResources
import kotlinx.android.synthetic.main.resource_item.view.*

class ResourcesAdapter(var resources: List<MyResources>?, var userLocation: Location?) :
    RecyclerView.Adapter<ResourcesAdapter.DoctorsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DoctorsViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.resource_item, parent, false)
        return DoctorsViewHolder(view)
    }
    override fun getItemCount() = resources?.size ?: 0

    override fun onBindViewHolder(viewHolder: DoctorsViewHolder, position: Int) {
        viewHolder.setArea(resources?.get(position))
    }

    inner class DoctorsViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

        fun checkIfNearMarker(resource: MyResources): Boolean {
            val docLocation = Location("Nearest Doctor")
            docLocation.latitude = resource.latitude.toDouble()
            docLocation.longitude = resource.longitude.toDouble()

            userLocation?.let {
                val distance = it.distanceTo(docLocation)
                if (distance >= 1000) {
                    view.distanceTV.text = view.context.getString(R.string.distanceKM, distance * 1000)
                } else view.distanceTV.text = view.context.getString(R.string.distanceM, distance)
                return distance < 1000
            }
            return false
        }

        fun setArea(resource: MyResources?) {
            resource?.let {
                view.resourceNameTV.text = it.name
                view.resourceAddressTV.text = it.reign
                view.resourceStreetTV.text = it.street
                if (it.resourceType == "doctors")
                    view.resourceHospitalTV.text = it.hospital
                else view.resourceHospitalTV.text = view.context.getString(R.string.pharmacy)
                setCardView(resource)
            }
        }

        fun setCardView(resource: MyResources) {
            view.imageView7.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
            if (userLocation != null) {
                if (checkIfNearMarker(resource)) {
                    view.imageView4?.setImageResource(R.drawable.ic_my_location_green_24dp)
                    view.detailsCV?.setOnClickListener {
                        val intent = Intent(view.context, FullDetailsActivity::class.java)
                        intent.putExtra("RESOURCE", resource)
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