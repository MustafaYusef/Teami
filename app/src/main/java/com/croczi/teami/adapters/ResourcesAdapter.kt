package com.croczi.teami.adapters

import android.content.Context
import android.content.Intent
import android.location.Location
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.Filter
import android.widget.Filterable
import android.widget.Toast
import com.croczi.teami.R
import com.croczi.teami.activities.FullDetailsActivity
import com.croczi.teami.models.MyResources
import com.wajahatkarim3.easyvalidation.core.collection_ktx.greaterThanList
import kotlinx.android.synthetic.main.resource_item.view.*

class ResourcesAdapter(
    var allResources: List<MyResources>?,
    var userLocation: Location?,
    var context: Context
) :
    RecyclerView.Adapter<ResourcesAdapter.DoctorsViewHolder>(){


    override fun getItemId(position: Int): Long {
        val resource = allResources?.get(position)
        var id: Long = 0
        resource?.id?.toLong()?.let {
            id = it
        }
        return id
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DoctorsViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.resource_item, parent, false)
        return DoctorsViewHolder(view)
    }

    override fun getItemCount() = allResources?.size?:0

    override fun onBindViewHolder(viewHolder: DoctorsViewHolder, position: Int) {
        viewHolder.setArea(allResources?.get(position))
    }

    inner class DoctorsViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

        fun checkIfNearMarker(resource: MyResources): Boolean {
            val docLocation = Location("Nearest Doctor")
            docLocation.latitude = resource.latitude.toDouble()
            docLocation.longitude = resource.longitude.toDouble()

            userLocation?.let {
                val distance = it.distanceTo(docLocation)
                if (distance in 1000.0..10000.0) {
                    view.distanceTV.text = view.context.getString(R.string.distanceKM, distance / 1000)
//                    return false
                } else if (distance < 1000) {
                    view.distanceTV.text = view.context.getString(R.string.distanceM, distance)
                }
//                else
                return distance < 150
            }
            return false
        }

        fun setArea(resource: MyResources?) {
            resource?.let {
                if (resource.resourceType == "pharmacies") {
                    view.resourceIV.setImageResource(R.drawable.ic_pharmacy_small)
                    view.resourceIV2.setImageResource(R.drawable.ic_pharmacy_red)
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
//                    view.detailsCV?.setOnClickListener(null)
                }
            } else view.imageView4?.setImageResource(R.drawable.ic_not_available)
            setAnimation(view, adapterPosition)
        }

        private var lastPosition = -1
        private val FADE_DURATION: Long = 500

        fun setAnimation(viewToAnimate: View, position: Int) {
            if (position > lastPosition) {
                val anim = ScaleAnimation(
                    0.5f,
                    1f,
                    0.5f,
                    1f,
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