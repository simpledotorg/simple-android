package org.simple.clinic.contactpatient.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.contactpatient_removeappointment.view.*
import org.simple.clinic.R
import org.simple.clinic.contactpatient.RemoveAppointmentReason
import org.simple.clinic.widgets.DividerItemDecorator
import org.simple.clinic.widgets.ItemAdapter
import org.simple.clinic.widgets.dp

class RemoveAppointmentView(
    context: Context,
    attributeSet: AttributeSet
) : ConstraintLayout(context, attributeSet) {

  private val removalReasonsAdapter = ItemAdapter(RemoveAppointmentReasonItem.DiffCallback())

  override fun onFinishInflate() {
    super.onFinishInflate()

    View.inflate(context, R.layout.contactpatient_removeappointment, this)

    removalReasonsRecyclerView.apply {
      setHasFixedSize(true)
      layoutManager = LinearLayoutManager(context)
      adapter = removalReasonsAdapter
      addItemDecoration(DividerItemDecorator(context, marginStart = 56.dp, marginEnd = 16.dp))
    }
  }

  fun renderAppointmentRemoveReasons(
      reasons: List<RemoveAppointmentReason>,
      selectedReason: RemoveAppointmentReason?
  ) {
    removalReasonsAdapter.submitList(RemoveAppointmentReasonItem.from(reasons, selectedReason))
  }

  fun enableRemoveAppointmentDoneButton() {
    removeAppointmentDone.isEnabled = true
  }

  fun disableRemoveAppointmentDoneButton() {
    removeAppointmentDone.isEnabled = false
  }
}
