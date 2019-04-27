package com.martin.teami.adapters

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import com.martin.teami.R
import com.martin.teami.models.Performance
import com.intrusoft.scatter.ChartData
import kotlinx.android.synthetic.main.history_item.view.*
import kotlinx.android.synthetic.main.performance_item.view.*
import kotlinx.android.synthetic.main.performance_item.view.classTV


class PerformanceAdapter(val performance: List<Performance>) :
    RecyclerView.Adapter<PerformanceAdapter.PerformanceVH>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PerformanceVH {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.performance_item, parent, false)
        return PerformanceVH(view)
    }

    override fun getItemCount() = performance.size

    override fun onBindViewHolder(viewHolder: PerformanceVH, position: Int) {
        viewHolder.setPerformance(performance[position])
    }

    class PerformanceVH(val view: View) : RecyclerView.ViewHolder(view) {

        fun setPerformance(performance: Performance) {
            view.classTV.text = performance.classX
            view.acceptedLvl.text = performance.acceptedLevel
            view.numDocs.text = performance.countOfDoctors.toString()
            view.ratioTV.text = "${performance.countOfVisit}/${performance.targetOfVisit}"
            val data = arrayListOf<ChartData>()
            data.add(
                ChartData(
                    "${performance.countOfVisit}/${performance.targetOfVisit}",
                    (performance.activationRatio.toFloat() * 100),
                    Color.WHITE,
                    Color.parseColor("#B514${Integer.toHexString((performance.activationRatio.toFloat() * 100 * 2.5).toInt())}")
                )
            )
            view.pieChart.setCenterCircleColor(Color.parseColor("#B514${Integer.toHexString((performance.activationRatio.toFloat() * 100 * 2.5).toInt())}"))
            view.pieChart.setChartData(data)
            view.pieChart.partitionWithPercent(true)
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