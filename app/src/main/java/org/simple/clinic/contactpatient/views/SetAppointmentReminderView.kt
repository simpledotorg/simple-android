package org.simple.clinic.contactpatient.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.android.synthetic.main.contactpatient_appointmentreminder.view.*
import org.simple.clinic.R

private typealias DecrementStepperClicked = () -> Unit
private typealias IncrementStepperClicked = () -> Unit
private typealias AppointmentDateClicked = () -> Unit
private typealias DoneClicked = () -> Unit

class SetAppointmentReminderView(
    context: Context,
    attributeSet: AttributeSet
) : ConstraintLayout(context, attributeSet) {

  var decrementStepperClicked: DecrementStepperClicked? = null

  var incrementStepperClicked: IncrementStepperClicked? = null

  var appointmentDateClicked: AppointmentDateClicked? = null

  var doneClicked: DoneClicked? = null

  override fun onFinishInflate() {
    super.onFinishInflate()

    View.inflate(context, R.layout.contactpatient_appointmentreminder, this)

    previousDateStepper.setOnClickListener { decrementStepperClicked?.invoke() }
    nextDateStepper.setOnClickListener { incrementStepperClicked?.invoke() }
    saveReminder.setOnClickListener { doneClicked?.invoke() }
    actualAppointmentDateButton.setOnClickListener { appointmentDateClicked?.invoke() }
  }
}
