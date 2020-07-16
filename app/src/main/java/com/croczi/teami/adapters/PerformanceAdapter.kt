package com.croczi.teami.adapters

import android.graphics.Color
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import com.croczi.teami.R
import com.croczi.teami.models.Performance
import com.intrusoft.scatter.ChartData
import kotlinx.android.synthetic.main.performance_item.view.*
import kotlinx.android.synthetic.main.performance_item.view.classTV


class PerformanceAdapter(val performance: List<Performance>) :
    androidx.recyclerview.widget.RecyclerView.Adapter<PerformanceAdapter.PerformanceVH>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PerformanceVH {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.performance_item, parent, false)
        return PerformanceVH(view)
    }

    override fun getItemCount() = performance.size

    override fun onBindViewHolder(viewHolder: PerformanceVH, position: Int) {
        viewHolder.setPerformance(performance[position])
    }

    class PerformanceVH(val view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {

        fun setPerformance(performance: Performance) {
            view.classTV.text = performance.classX
            view.acceptedLvl.text = performance.acceptedLevel
            view.numDocs.text = performance.countOfDoctors.toString()
            var ratio = ((performance.countOfVisit.toFloat() / performance.targetOfVisit.toFloat()) * 100).toInt()
            if(ratio>100)
                ratio=100
            val data = arrayListOf<ChartData>()
            data.add(
                ChartData(
                    "$ratio%",
                    ratio.toFloat(),
                    Color.WHITE
                    ,Color.parseColor("#871E20")
                )
            )
            data.add(ChartData("",100f,Color.parseColor("#B5292C"),Color.parseColor("#B5292C")))
            view.pieChart.setCenterCircleColor(Color.parseColor("#ff3B35"))
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