package org.simple.clinic.home.report

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout

class ReportsScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs){

  companion object {
    val KEY = ReportsScreenKey()
  }

  override fun onFinishInflate() {
    super.onFinishInflate()

    if (isInEditMode) {
      return
    }
  }
}
