package com.martin.teami.adapters

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import com.martin.teami.fragments.fragmentsUtils.SmartFragmentStatePagerAdapter

class BottomBarAdapter(fragmentManager: FragmentManager, private val fragmentsList: List<Fragment>) : SmartFragmentStatePagerAdapter(fragmentManager) {

    override fun getItem(position: Int): Fragment {
        return fragmentsList[position]
    }

    override fun getCount(): Int {
        return fragmentsList.size
    }
}