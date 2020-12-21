package org.simple.clinic.contactpatient.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.jakewharton.rxbinding3.view.detaches
import io.reactivex.rxkotlin.ofType
import kotlinx.android.synthetic.main.contactpatient_removeappointment.view.*
import org.simple.clinic.R
import org.simple.clinic.contactpatient.RemoveAppointmentReason
import org.simple.clinic.databinding.ContactpatientRemoveappointmentReasonitemBinding
import org.simple.clinic.widgets.ItemAdapter
import org.simple.clinic.widgets.DividerItemDecorator
import org.simple.clinic.widgets.dp

private typealias RemoveReasonClicked = (RemoveAppointmentReason) -> Unit
private typealias RemoveAppointmentDoneClicked = () -> Unit
private typealias RemoveAppointmentCloseClicked = () -> Unit

class RemoveAppointmentView(
    context: Context,
    attributeSet: AttributeSet
) : ConstraintLayout(context, attributeSet) {

  private val removalReasonsAdapter = ItemAdapter(
      diffCallback = RemoveAppointmentReasonItem.DiffCallback(),
      bindings = mapOf(
          R.layout.contactpatient_removeappointment_reasonitem to { layoutInflater, parent ->
            ContactpatientRemoveappointmentReasonitemBinding.inflate(layoutInflater, parent, false)
          }
      )
  )

  var removeReasonClicked: RemoveReasonClicked? = null

  var doneClicked: RemoveAppointmentDoneClicked? = null

  var closeClicked: RemoveAppointmentCloseClicked? = null

  @SuppressLint("CheckResult")
  override fun onFinishInflate() {
    super.onFinishInflate()

    View.inflate(context, R.layout.contactpatient_removeappointment, this)

    removalReasonsRecyclerView.apply {
      setHasFixedSize(true)
      layoutManager = LinearLayoutManager(context)
      adapter = removalReasonsAdapter
      addItemDecoration(DividerItemDecorator(context, marginStart = 56.dp, marginEnd = 16.dp))
    }

    removeAppointmentDone.setOnClickListener { doneClicked?.invoke() }
    removeAppointmentToolbar.setNavigationOnClickListener { closeClicked?.invoke() }
    removalReasonsAdapter
        .itemEvents
        .takeUntil(detaches())
        .ofType<RemoveAppointmentReasonItem.Event.Clicked>()
        .subscribe { removeReasonClicked?.invoke(it.reason) }
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
