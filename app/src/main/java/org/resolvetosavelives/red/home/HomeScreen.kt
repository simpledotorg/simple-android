package org.resolvetosavelives.red.home

import android.content.Context
import android.support.design.widget.TabLayout
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.widget.RelativeLayout
import kotterknife.bindView
import org.resolvetosavelives.red.R

class HomeScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

  companion object {
    val KEY = HomeScreenKey()
  }

  private val viewPagerTabs by bindView<TabLayout>(R.id.home_viewpager_tabs)

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }

    for (tabTitle in arrayOf("New BP", "Call list", "Reports")) {
      val tab = viewPagerTabs.newTab()
      tab.text = tabTitle
      viewPagerTabs.addTab(tab)
    }
  }
}
