package org.simple.clinic.patientcontact

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import org.simple.clinic.R

class CallPatientView(
    context: Context,
    attributeSet: AttributeSet
) : ConstraintLayout(context, attributeSet) {

  override fun onFinishInflate() {
    super.onFinishInflate()

    View.inflate(context, R.layout.patientcontact_view_callpatient, this)
  }
}
