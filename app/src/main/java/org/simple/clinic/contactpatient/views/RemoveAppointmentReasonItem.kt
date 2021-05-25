package org.simple.clinic.contactpatient.views

import androidx.recyclerview.widget.DiffUtil
import io.reactivex.subjects.Subject
import org.simple.clinic.R
import org.simple.clinic.contactpatient.RemoveAppointmentReason
import org.simple.clinic.databinding.RemoveappointmentReasonitemBinding
import org.simple.clinic.widgets.ItemAdapter
import org.simple.clinic.widgets.recyclerview.BindingViewHolder
import org.simple.clinic.widgets.visibleOrGone

data class RemoveAppointmentReasonItem(
    private val reason: RemoveAppointmentReason,
    private val isSelected: Boolean,
    private val showDivider: Boolean
) : ItemAdapter.Item<RemoveAppointmentReasonItem.Event> {

  companion object {

    fun from(
        reasons: List<RemoveAppointmentReason>,
        selected: RemoveAppointmentReason?
    ): List<RemoveAppointmentReasonItem> {
      return reasons.mapIndexed { index, reason ->
        RemoveAppointmentReasonItem(
            reason = reason,
            isSelected = reason == selected,
            showDivider = index != reasons.lastIndex
        )
      }
    }
  }

  override fun layoutResId(): Int = R.layout.removeappointment_reasonitem

  override fun render(holder: BindingViewHolder, subject: Subject<Event>) {
    val binding = holder.binding as RemoveappointmentReasonitemBinding

    binding.removalReasonButton.setText(reason.displayText)
    binding.removalReasonButton.isChecked = isSelected
    binding.removalReasonButton.setOnClickListener { subject.onNext(Event.Clicked(reason)) }
    binding.divider.visibleOrGone(showDivider)
  }

  sealed class Event {
    data class Clicked(val reason: RemoveAppointmentReason) : Event()
  }

  class DiffCallback : DiffUtil.ItemCallback<RemoveAppointmentReasonItem>() {

    override fun areItemsTheSame(
        oldItem: RemoveAppointmentReasonItem,
        newItem: RemoveAppointmentReasonItem
    ): Boolean {
      return oldItem.reason == newItem.reason
    }

    override fun areContentsTheSame(
        oldItem: RemoveAppointmentReasonItem,
        newItem: RemoveAppointmentReasonItem
    ): Boolean {
      return oldItem == newItem
    }
  }
}
