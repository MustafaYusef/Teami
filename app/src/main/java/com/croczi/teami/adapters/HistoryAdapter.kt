package com.croczi.teami.adapters

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import com.croczi.teami.R
import com.croczi.teami.models.History
import kotlinx.android.synthetic.main.history_item.view.*
import com.intrusoft.scatter.ChartData


class HistoryAdapter(val history: List<History>) :
    RecyclerView.Adapter<HistoryAdapter.HistoryVH>() {
    override fun onViewDetachedFromWindow(holder: HistoryVH) {
        super.onViewDetachedFromWindow(holder)
        holder.itemView.clearAnimation()

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryVH {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.history_item, parent, false)
        return HistoryVH(view)
    }

    override fun getItemCount() = history.size

    override fun onBindViewHolder(viewHolder: HistoryVH, position: Int) {
        viewHolder.setOrder(history[position])
    }

    class HistoryVH(val view: View) : RecyclerView.ViewHolder(view) {

        fun setOrder(history: History) {
            view.resourceNameTV.text = history.doctorName
            view.classTV.text = history.classX
            view.specialityTV.text = history.speciality
            var ratio = ((history.countOfVisit.toFloat() / history.targetOfVisit.toFloat()) * 100).toInt()
            if (ratio > 100)
                ratio = 100
            val data = arrayListOf<ChartData>()
            data.add(
                ChartData(
                    "$ratio%",
                    ratio.toFloat(),
                    Color.WHITE
                    , Color.parseColor("#871E20")
                )
            )
            data.add(ChartData("", 100f, Color.parseColor("#B5292C"), Color.parseColor("#B5292C")))
            view.animatedPieView.setCenterCircleColor(Color.parseColor("#ff3B35"))
            view.targetTV.text = "Target visits: ${history.targetOfVisit}"
            view.visitsTV.text = "Actual visits: ${history.countOfVisit}"
            view.animatedPieView.setChartData(data)
            view.animatedPieView.partitionWithPercent(true)
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