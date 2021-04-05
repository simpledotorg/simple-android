package org.simple.clinic.home

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class HomeScreenTabPagerAdapter(
    activity: AppCompatActivity,
    private val screens: List<HomeTab>
) : FragmentStateAdapter(activity) {

  override fun getItemCount(): Int = screens.size

  override fun createFragment(position: Int): Fragment = screens[position].key.createFragment()
}
