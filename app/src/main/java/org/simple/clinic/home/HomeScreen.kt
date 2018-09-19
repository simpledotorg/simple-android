package org.simple.clinic.home

import android.content.Context
import android.support.v4.view.ViewPager
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
  private val viewPager by bindView<ViewPager>(R.id.home_viewpager)

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }

    // Keyboard stays open after login finishes, not sure why.
    rootLayout.hideKeyboard()

    viewPager.adapter = HomePagerAdapter(context)
  }
}
