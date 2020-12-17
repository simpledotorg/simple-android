package org.simple.clinic.bp.history.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.text.style.TextAppearanceSpan
import io.reactivex.subjects.Subject
import org.simple.clinic.R
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.bp.history.adapter.Event.AddNewBpClicked
import org.simple.clinic.bp.history.adapter.Event.BloodPressureHistoryItemClicked
import org.simple.clinic.databinding.ListBpHistoryItemBinding
import org.simple.clinic.databinding.ListNewBpButtonBinding
import org.simple.clinic.util.Truss
import org.simple.clinic.widgets.PagingItemAdapter
import org.simple.clinic.widgets.recyclerview.BindingViewHolder
import org.simple.clinic.widgets.visibleOrGone

sealed class BloodPressureHistoryListItem : PagingItemAdapter.Item<Event> {

  object NewBpButton : BloodPressureHistoryListItem() {

    override fun layoutResId(): Int = R.layout.list_new_bp_button

    override fun render(holder: BindingViewHolder, subject: Subject<Event>) {
      val binding = holder.binding as ListNewBpButtonBinding
      binding.newBpButton.setOnClickListener { subject.onNext(AddNewBpClicked) }
    }
  }

  data class BloodPressureHistoryItem(
      val measurement: BloodPressureMeasurement,
      val isBpEditable: Boolean,
      val isBpHigh: Boolean,
      val bpDate: String,
      val bpTime: String?
  ) : BloodPressureHistoryListItem() {

    override fun layoutResId(): Int = R.layout.list_bp_history_item

    @SuppressLint("SetTextI18n")
    override fun render(holder: BindingViewHolder, subject: Subject<Event>) {
      val binding = holder.binding as ListBpHistoryItemBinding
      val context = holder.itemView.context
      val formattedBPDateTime = Truss()
          .pushSpan(dateTimeTextAppearance(context))
          .append(bloodPressureDateTime(context))
          .popSpan()
          .build()

      if (isBpHigh) {
        binding.heartImageView.setImageResource(R.drawable.bp_reading_high)
      } else {
        binding.heartImageView.setImageResource(R.drawable.bp_reading_normal)
      }
      binding.bpHighTextView.visibleOrGone(isBpHigh)

      if (isBpEditable) {
        holder.itemView.setOnClickListener { subject.onNext(BloodPressureHistoryItemClicked(measurement)) }
      } else {
        holder.itemView.setOnClickListener(null)
      }
      holder.itemView.isClickable = isBpEditable
      holder.itemView.isFocusable = isBpEditable
      binding.editButton.visibleOrGone(isBpEditable)

      binding.readingsTextView.text = "${measurement.reading.systolic} / ${measurement.reading.diastolic}"
      binding.timeDateTextView.text = formattedBPDateTime
    }

    private fun bloodPressureDateTime(context: Context): String {
      return if (bpTime != null) {
        context.getString(R.string.bloodpressurehistory_bp_time_date, bpDate, bpTime)
      } else {
        bpDate
      }
    }

    private fun dateTimeTextAppearance(context: Context?): TextAppearanceSpan {
      return if (bpTime != null) {
        TextAppearanceSpan(context, R.style.Clinic_V2_TextAppearance_Caption_Grey1)
      } else {
        TextAppearanceSpan(context, R.style.Clinic_V2_TextAppearance_Body2Left_Grey1)
      }
    }
  }
}
