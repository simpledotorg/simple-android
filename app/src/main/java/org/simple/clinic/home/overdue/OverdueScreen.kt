package org.simple.clinic.home.overdue

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout

class OverdueScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {


  companion object {
    val KEY = OverdueScreenKey()
  }

  override fun onFinishInflate() {
    super.onFinishInflate()

    if (isInEditMode) {
      return
    }


  }



}


