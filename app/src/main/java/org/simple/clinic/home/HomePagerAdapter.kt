package org.simple.clinic.home

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter

class HomePagerAdapter(
    private val context: Context,
    private val tabs: List<HomeTab>
) : PagerAdapter() {

  override fun instantiateItem(container: ViewGroup, position: Int): Any {
    val inflater = LayoutInflater.from(container.context)

    val screenForTab = inflater.inflate(tabs[position].key.layoutRes(), container, false)
    container.addView(screenForTab)

    return screenForTab
  }

  override fun destroyItem(container: ViewGroup, position: Int, viewKey: Any) {
    container.removeView(viewKey as View)
  }

  override fun getPageTitle(position: Int): CharSequence = context.resources.getString(tabs[position].title)

  override fun getCount() = tabs.size

  override fun isViewFromObject(view: View, viewKey: Any) = view === viewKey
}
