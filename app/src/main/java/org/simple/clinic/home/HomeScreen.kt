package org.simple.clinic.home

import android.content.Context
import android.support.design.widget.TabLayout
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.RelativeLayout
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.widgets.hideKeyboard

class HomeScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

  companion object {
    val KEY = HomeScreenKey()
  }

  private val rootLayout by bindView<ViewGroup>(R.id.home_root)
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

    // Keyboard stays open after login finishes, not sure why.
    rootLayout.hideKeyboard()
  }
}
