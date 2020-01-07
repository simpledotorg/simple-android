package org.simple.clinic.summary.bloodsugar.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import kotlinx.android.synthetic.main.patientsummary_bpplaceholderitem_content.view.*
import org.simple.clinic.R

class BloodSugarPlaceholderItemView(
    context: Context,
    attributeSet: AttributeSet
) : FrameLayout(context, attributeSet) {

  init {
    LayoutInflater.from(context).inflate(R.layout.patientsummary_bpplaceholderitem_content, this, true)
  }

  fun render(showHint: Boolean) {
    placeHolderMessageTextView.visibility = if (showHint) VISIBLE else INVISIBLE
  }
}
