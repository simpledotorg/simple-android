package org.resolvetosavelives.red.home

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import kotlinx.android.synthetic.main.screen_home.view.*
import org.resolvetosavelives.red.TheActivity
import org.resolvetosavelives.red.newentry.search.PatientMobileEntryScreen
import timber.log.Timber

class HomeScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

  companion object {
    val KEY = HomeScreenKey()
  }

  override fun onFinishInflate() {
    super.onFinishInflate()

    mobileButton.setOnClickListener({
      Timber.i("Going to patient mobile entry")
      TheActivity.screenRouter().push(PatientMobileEntryScreen.KEY)
    })
  }
}
