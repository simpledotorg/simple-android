package org.simple.clinic.widgets

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import org.simple.clinic.R

class FindOrRegisterPatientToolbar(
    context: Context,
    attributeSet: AttributeSet
) : FrameLayout(context, attributeSet) {

  override fun onFinishInflate() {
    super.onFinishInflate()
    inflate(context, R.layout.view_findorregisterpatient_toolbar, this)
  }
}
