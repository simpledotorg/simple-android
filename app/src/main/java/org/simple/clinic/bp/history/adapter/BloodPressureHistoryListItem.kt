package org.simple.clinic.bp.history.adapter

import android.annotation.SuppressLint
import android.text.style.TextAppearanceSpan
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.list_bp_history_item.*
import kotlinx.android.synthetic.main.list_new_bp_button.*
import org.simple.clinic.R
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.bp.history.adapter.Event.AddNewBpClicked
import org.simple.clinic.bp.history.adapter.Event.BloodPressureHistoryItemClicked
import org.simple.clinic.util.Truss
import org.simple.clinic.widgets.PagingItemAdapter
import org.simple.clinic.widgets.recyclerview.ViewHolderX
import org.simple.clinic.widgets.visibleOrGone

sealed class BloodPressureHistoryListItem : PagingItemAdapter.Item<Event> {

  object NewBpButton : BloodPressureHistoryListItem() {

    override fun layoutResId(): Int = R.layout.list_new_bp_button

    override fun render(holder: ViewHolderX, subject: Subject<Event>) {
      holder.newBpButton.setOnClickListener { subject.onNext(AddNewBpClicked) }
    }
  }

  data class BloodPressureHistoryItem(
      val measurement: BloodPressureMeasurement,
      val isBpEditable: Boolean,
      val isHighBloodPressure: Boolean,
      val bpDate: String,
      val bpTime: String?
  ) : BloodPressureHistoryListItem() {

    override fun layoutResId(): Int = R.layout.list_bp_history_item

    @SuppressLint("SetTextI18n")
    override fun render(holder: ViewHolderX, subject: Subject<Event>) {
      val context = holder.itemView.context
      val bpDateTime = if (bpTime != null) {
        context.getString(R.string.bloodpressurehistory_bp_time_date, bpDate, bpTime)
      } else {
        bpDate
      }
      val dateTimeTextAppearance = if (bpTime != null) {
        TextAppearanceSpan(context, R.style.Clinic_V2_TextAppearance_Caption_Grey1)
      } else {
        TextAppearanceSpan(context, R.style.Clinic_V2_TextAppearance_Body2Left_Grey1)
      }

      val formattedBPDateTime = Truss()
          .pushSpan(dateTimeTextAppearance)
          .append(bpDateTime)
          .popSpan()
          .build()

      if (isHighBloodPressure) {
        holder.heartImageView.setImageResource(R.drawable.bp_reading_high)
      } else {
        holder.heartImageView.setImageResource(R.drawable.bp_reading_normal)
      }
      holder.bpHighTextView.visibleOrGone(isHighBloodPressure)

      if (isBpEditable) {
        holder.itemView.setOnClickListener { subject.onNext(BloodPressureHistoryItemClicked(measurement)) }
      } else {
        holder.itemView.setOnClickListener(null)
      }
      holder.itemView.isClickable = isBpEditable
      holder.itemView.isFocusable = isBpEditable
      holder.editButton.visibleOrGone(isBpEditable)

      holder.readingsTextView.text = "${measurement.systolic} / ${measurement.diastolic}"
      holder.timeDateTextView.text = formattedBPDateTime
    }
  }
}
