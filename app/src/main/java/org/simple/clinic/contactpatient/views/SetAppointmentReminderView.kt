package org.simple.clinic.contactpatient.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import org.simple.clinic.R

class SetAppointmentReminderView(
    context: Context,
    attributeSet: AttributeSet
) : ConstraintLayout(context, attributeSet) {

  override fun onFinishInflate() {
    super.onFinishInflate()

    View.inflate(context, R.layout.contactpatient_appointmentreminder, this)
  }
}
