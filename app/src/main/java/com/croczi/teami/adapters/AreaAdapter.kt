package com.croczi.teami.adapters

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import com.croczi.teami.R
import com.croczi.teami.models.CoverageArea
import kotlinx.android.synthetic.main.area_item.view.*

class AreaAdapter(val areas: List<CoverageArea>) :
    androidx.recyclerview.widget.RecyclerView.Adapter<AreaAdapter.AreaViewHolder>() {
    override fun onViewDetachedFromWindow(holder: AreaViewHolder) {
        super.onViewDetachedFromWindow(holder)
        holder.itemView.clearAnimation()

    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AreaViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.area_item, parent, false)
        return AreaViewHolder(view)
    }

    override fun getItemCount() = areas.size

    override fun onBindViewHolder(viewHolder: AreaViewHolder, position: Int) {
        viewHolder.setArea(areas[position])
    }

    class AreaViewHolder(val view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {

        fun setArea(area: CoverageArea) {
            view.areaTV.text=area.name
            setAnimation(view,adapterPosition)
        }
        private var lastPosition = -1
        private val FADE_DURATION: Long = 700

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
                anim.duration = FADE_DURATION//to make duration random number between [0,501)
                viewToAnimate.startAnimation(anim)
                lastPosition = position
            }
        }
    }
}