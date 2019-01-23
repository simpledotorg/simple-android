package org.simple.clinic.home.report

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import java.net.URI

class ReportsScreen(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

  companion object {
    val KEY = ReportsScreenKey()
  }

  override fun onFinishInflate() {
    super.onFinishInflate()

    if (isInEditMode) {
      return
    }
  }

  fun showReport(uri: URI) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  fun showNoReportsAvailable() {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }
}
