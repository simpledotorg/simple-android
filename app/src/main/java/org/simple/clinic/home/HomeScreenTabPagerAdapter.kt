package org.simple.clinic.home

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class HomeScreenTabPagerAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle,
    private val screens: List<HomeTab>
) : FragmentStateAdapter(fragmentManager, lifecycle) {

  override fun getItemCount(): Int = screens.size

  override fun createFragment(position: Int): Fragment = screens[position].key.createFragment()
}
