package com.croczi.teami.adapters

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import com.croczi.teami.R
import com.croczi.teami.models.UserOrder
import kotlinx.android.synthetic.main.user_orders_item.view.*

class UserOrdersAdapter(val orders: List<UserOrder>) :
    androidx.recyclerview.widget.RecyclerView.Adapter<UserOrdersAdapter.UserOrdersVH>() {
    override fun onViewDetachedFromWindow(holder: UserOrdersVH) {
        super.onViewDetachedFromWindow(holder)
        holder.itemView.clearAnimation()

    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserOrdersVH {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.user_orders_item, parent, false)
        return UserOrdersVH(view)
    }

    override fun getItemCount() = orders.size

    override fun onBindViewHolder(viewHolder: UserOrdersVH, position: Int) {
        viewHolder.setOrder(orders[position])
    }

    class UserOrdersVH(val view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {

        fun setOrder(order: UserOrder) {
            view.pharmacyNameTV.text=order.pharmacyName
            view.itemTypeCountTV.text=order.countOfItemType.toString()
            view.itemsCount.text=order.countOfItem.toString()
            setAnimation(view,adapterPosition)
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