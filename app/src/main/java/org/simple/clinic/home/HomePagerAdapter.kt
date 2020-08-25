package org.simple.clinic.home

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter

class HomePagerAdapter(private val context: Context) : PagerAdapter() {

  override fun instantiateItem(container: ViewGroup, position: Int): Any {
    val inflater = LayoutInflater.from(container.context)

    val screenForTab = inflater.inflate(HomeTab.values()[position].key.layoutRes(), container, false)
    container.addView(screenForTab)

    return screenForTab
  }

  override fun destroyItem(container: ViewGroup, position: Int, viewKey: Any) {
    container.removeView(viewKey as View)
  }

  override fun getPageTitle(position: Int): CharSequence = context.resources.getString(HomeTab.values()[position].title)

  override fun getCount() = HomeTab.values().size

  override fun isViewFromObject(view: View, viewKey: Any) = view === viewKey
}
