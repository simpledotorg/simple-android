package org.simple.clinic.editpatient

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout

class PatientEditScreen(context: Context, attributeSet: AttributeSet) : RelativeLayout(context, attributeSet) {

  companion object {
    @JvmField
    val KEY = ::PatientEditScreenKey
  }

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }
  }
}
