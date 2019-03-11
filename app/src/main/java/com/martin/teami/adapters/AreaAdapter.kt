package com.martin.teami.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.martin.teami.R
import com.martin.teami.models.CoverageArea
import com.martin.teami.models.Resource
import kotlinx.android.synthetic.main.area_item.view.*

class AreaAdapter(val areas: List<CoverageArea>) :
    RecyclerView.Adapter<AreaAdapter.AreaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AreaViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.area_item, parent, false)
        return AreaViewHolder(view)
    }

    override fun getItemCount() = areas.size

    override fun onBindViewHolder(viewHolder: AreaViewHolder, position: Int) {
        viewHolder.setArea(areas[position])
    }

    class AreaViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

        fun setArea(area: CoverageArea) {
            view.areaTV.text=area.name
        }
    }
}