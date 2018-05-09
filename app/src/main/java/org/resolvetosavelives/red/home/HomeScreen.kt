package org.resolvetosavelives.red.home

import android.content.Context
import android.util.AttributeSet
import android.widget.Button
import android.widget.RelativeLayout
import org.resolvetosavelives.red.R
import org.resolvetosavelives.red.TheActivity
import org.resolvetosavelives.red.newentry.PatientMobileEntryScreen
import timber.log.Timber

class HomeScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

  companion object {
    val KEY = HomeScreenKey()
  }

  init {
    Timber.i("HomeScreen()")
  }

  override fun onFinishInflate() {
    super.onFinishInflate()

    Timber.i("onFinishInflate")

    val mobileButon = findViewById<Button>(R.id.homeMobile)
    mobileButon.setOnClickListener({
      Timber.i("Going to patient mobile entry")
      TheActivity.screenRouter().push(PatientMobileEntryScreen.KEY)
    })
  }
}
