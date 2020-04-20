package org.simple.clinic.contactpatient.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.android.synthetic.main.contactpatient_removeappointment.view.*
import org.simple.clinic.R
import org.simple.clinic.widgets.DividerItemDecorator
import org.simple.clinic.widgets.dp

class RemoveAppointmentView(
    context: Context,
    attributeSet: AttributeSet
) : ConstraintLayout(context, attributeSet) {

  override fun onFinishInflate() {
    super.onFinishInflate()

    View.inflate(context, R.layout.contactpatient_removeappointment, this)

    removalReasonsRecyclerView.addItemDecoration(DividerItemDecorator(context, marginStart = 56.dp, marginEnd = 16.dp))
  }
}
