package com.croczi.teami.adapters

import android.content.Intent
import android.location.Location
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.Toast
import com.croczi.teami.R
import com.croczi.teami.activities.FullDetailsActivity
import com.croczi.teami.models.MyResources
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
                if (distance in 30.0..10000.0) {
                    view.distanceTV.text = view.context.getString(R.string.distanceKM, distance / 1000)
                } else if (distance < 30) {
                    view.distanceTV.text = view.context.getString(R.string.distanceM, distance)
                    return true
                } else
                    return distance < 30
            }
            return false
        }

        fun setArea(resource: MyResources?) {
            resource?.let {
                if (resource.resourceType == "pharmacies") {
                    view.resourceIV.setImageResource(R.drawable.ic_pharmacy_small)
                }
                view.resourceNameTV.text = it.name
                view.resourceAddressTV.text = it.reign
                view.resourceStreetTV.text = it.street
                view.resourceHospitalTV.text = it.organisation
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
                    view.detailsCV?.setOnClickListener {
                        Toast.makeText(view.context, view.context.getString(R.string.not_near), Toast.LENGTH_LONG)
                            .show()
                    }
                    view.imageView4?.setImageResource(R.drawable.ic_my_location_red_24dp)
                    view.detailsCV?.setOnClickListener(null)
                }
            } else view.imageView4?.setImageResource(R.drawable.ic_not_available)
            setAnimation(view, adapterPosition)
        }

        private var lastPosition = -1
        private val FADE_DURATION: Long = 500

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
                anim.duration = FADE_DURATION
                viewToAnimate.startAnimation(anim)
                lastPosition = position
            }
        }
    }

}