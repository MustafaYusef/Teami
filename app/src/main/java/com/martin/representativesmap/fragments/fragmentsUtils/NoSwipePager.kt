package com.martin.representativesmap.fragments.fragmentsUtils

import android.content.Context
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.MotionEvent

class NoSwipePager(context: Context, attrs: AttributeSet) : ViewPager(context, attrs) {
    private var checked: Boolean = false

    init {
        this.checked = true
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return if (this.checked) {
            super.onTouchEvent(event)
        } else false
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        return if (this.checked) {
            super.onInterceptTouchEvent(event)
        } else false
    }

    fun setPagingEnabled(checked: Boolean) {
        this.checked = checked
    }
}