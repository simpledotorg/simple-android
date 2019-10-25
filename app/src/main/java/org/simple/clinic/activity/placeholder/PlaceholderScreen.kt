package org.simple.clinic.activity.placeholder

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout

class PlaceholderScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {
  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }
  }
}
