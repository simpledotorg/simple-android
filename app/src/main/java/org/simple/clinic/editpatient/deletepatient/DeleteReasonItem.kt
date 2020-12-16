package org.simple.clinic.editpatient.deletepatient

import androidx.recyclerview.widget.DiffUtil
import io.reactivex.subjects.Subject
import org.simple.clinic.R
import org.simple.clinic.databinding.ListDeleteReasonBinding
import org.simple.clinic.widgets.BindingItemAdapter
import org.simple.clinic.widgets.recyclerview.BindingViewHolder

data class DeleteReasonItem(
    val reason: PatientDeleteReason,
    val isSelected: Boolean
) : BindingItemAdapter.Item<DeleteReasonItem.Event> {

  companion object {

    fun from(
        deleteReasons: List<PatientDeleteReason>,
        selectedReason: PatientDeleteReason?
    ): List<DeleteReasonItem> {
      return deleteReasons.map { deleteReason ->
        DeleteReasonItem(
            reason = deleteReason,
            isSelected = deleteReason == selectedReason
        )
      }
    }
  }

  sealed class Event {
    data class Clicked(val reason: PatientDeleteReason) : Event()
  }

  override fun layoutResId(): Int = R.layout.list_delete_reason

  override fun render(holder: BindingViewHolder, subject: Subject<Event>) {
    val binding = holder.binding as ListDeleteReasonBinding

    binding.deleteReasonRadioButton.setText(reason.displayText)
    binding.deleteReasonRadioButton.isChecked = isSelected
    binding.deleteReasonRadioButton.setOnClickListener { subject.onNext(Event.Clicked(reason)) }
  }

  class DiffCallback : DiffUtil.ItemCallback<DeleteReasonItem>() {
    override fun areItemsTheSame(oldItem: DeleteReasonItem, newItem: DeleteReasonItem): Boolean {
      return oldItem.reason == newItem.reason
    }

    override fun areContentsTheSame(oldItem: DeleteReasonItem, newItem: DeleteReasonItem): Boolean {
      return oldItem == newItem
    }
  }
}
