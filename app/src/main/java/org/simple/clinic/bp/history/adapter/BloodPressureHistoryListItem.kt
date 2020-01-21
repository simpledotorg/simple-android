package org.simple.clinic.bp.history.adapter

import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.list_bp_history_item.*
import kotlinx.android.synthetic.main.list_bp_history_item.divider
import kotlinx.android.synthetic.main.list_new_bp_button.*
import org.simple.clinic.R
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.bp.history.adapter.Event.AddNewBpClicked
import org.simple.clinic.bp.history.adapter.Event.BloodPressureHistoryItemClicked
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.toLocalDateAtZone
import org.simple.clinic.widgets.ItemAdapter
import org.simple.clinic.widgets.recyclerview.ViewHolderX
import org.simple.clinic.widgets.visibleOrGone
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import org.threeten.bp.format.DateTimeFormatter

sealed class BloodPressureHistoryListItem : ItemAdapter.Item<Event> {
  companion object {
    fun from(
        measurements: List<BloodPressureMeasurement>,
        canEditFor: Duration,
        utcClock: UtcClock,
        userClock: UserClock,
        dateFormatter: DateTimeFormatter,
        timeFormatter: DateTimeFormatter
    ): List<BloodPressureHistoryListItem> {
      val measurementsByDate = measurements.groupBy { it.recordedAt.toLocalDateAtZone(userClock.zone) }

      val bpHistoryItems = measurementsByDate.mapValues { (_, measurementsList) ->
        if (measurementsList.size > 1) {
          measurementsList.map { measurement ->
            val isBpEditable = isBpEditable(measurement, canEditFor, utcClock)
            val recordedAt = measurement.recordedAt.toLocalDateAtZone(userClock.zone)

            BloodPressureHistoryItem(
                measurement = measurement,
                isBpEditable = isBpEditable,
                isHighBloodPressure = measurement.level.isUrgent(),
                showDivider = true,
                bpDate = dateFormatter.format(recordedAt),
                bpTime = timeFormatter.format(measurement.recordedAt.atZone(userClock.zone))
            )
          }
        } else {
          measurementsList.map { measurement ->
            val isBpEditable = isBpEditable(measurement, canEditFor, utcClock)
            val recordedAt = measurement.recordedAt.toLocalDateAtZone(userClock.zone)

            BloodPressureHistoryItem(
                measurement = measurement,
                isBpEditable = isBpEditable,
                isHighBloodPressure = measurement.level.isUrgent(),
                showDivider = true,
                bpDate = dateFormatter.format(recordedAt),
                bpTime = null
            )
          }
        }
      }.values.flatten()

      return listOf(NewBpButton) + bpHistoryItems.mapIndexed { index, measurement ->
        val isLastIndex = index == bpHistoryItems.lastIndex
        if (isLastIndex) {
          measurement.copy(showDivider = false)
        } else {
          measurement
        }
      }
    }

    private fun isBpEditable(
        bloodPressureMeasurement: BloodPressureMeasurement,
        bpEditableFor: Duration,
        utcClock: UtcClock
    ): Boolean {
      val now = Instant.now(utcClock)
      val createdAt = bloodPressureMeasurement.createdAt

      val durationSinceBpCreated = Duration.between(createdAt, now)

      return durationSinceBpCreated <= bpEditableFor
    }
  }

  object NewBpButton : BloodPressureHistoryListItem() {

    override fun layoutResId(): Int = R.layout.list_new_bp_button

    override fun render(holder: ViewHolderX, subject: Subject<Event>) {
      holder.newBp.setOnClickListener { subject.onNext(AddNewBpClicked) }
    }
  }

  data class BloodPressureHistoryItem(
      val measurement: BloodPressureMeasurement,
      val isBpEditable: Boolean,
      val isHighBloodPressure: Boolean,
      val showDivider: Boolean,
      val bpDate: String,
      val bpTime: String?
  ) : BloodPressureHistoryListItem() {

    override fun layoutResId(): Int = R.layout.list_bp_history_item

    override fun render(holder: ViewHolderX, subject: Subject<Event>) {
      val context = holder.itemView.context
      val bpTimeDate = if (bpTime != null) {
        context.getString(R.string.bloodpressurehistory_bp_time_date, bpTime, bpDate)
      } else {
        bpDate
      }

      if (isHighBloodPressure) {
        holder.heartImageView.setImageResource(R.drawable.bp_reading_high)
      } else {
        holder.heartImageView.setImageResource(R.drawable.bp_reading_normal)
      }

      if (isBpEditable) {
        holder.itemView.setOnClickListener { subject.onNext(BloodPressureHistoryItemClicked(measurement)) }
      } else {
        holder.itemView.setOnClickListener(null)
      }
      holder.itemView.isClickable = isBpEditable
      holder.itemView.isFocusable = isBpEditable
      holder.editButton.visibleOrGone(isBpEditable)

      holder.readingsTextView.text = context.getString(R.string.bloodpressurehistory_bp_reading, measurement.systolic, measurement.diastolic)
      holder.timeDateTextView.text = bpTimeDate
      holder.divider.visibleOrGone(showDivider)
    }
  }
}
