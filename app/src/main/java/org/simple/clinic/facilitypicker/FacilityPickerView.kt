package org.simple.clinic.facilitypicker

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import org.simple.clinic.R

class FacilityPickerView(
    context: Context,
    attributeSet: AttributeSet
) : FrameLayout(context, attributeSet) {

  init {
    inflate(context, R.layout.view_facilitypicker, this)
  }
}
