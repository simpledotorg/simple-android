package org.simple.clinic.summary

import android.view.View
import androidx.annotation.VisibleForTesting
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import io.reactivex.subjects.Subject
import kotlinx.android.extensions.LayoutContainer
import org.simple.clinic.R
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.summary.bloodpressures.BloodPressureItemView
import org.simple.clinic.util.RelativeTimestamp
import org.simple.clinic.util.RelativeTimestampGenerator
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.UtcClock
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter

data class SummaryBloodPressureListItem(
    val measurement: BloodPressureMeasurement,
    val showDivider: Boolean,
    val formattedTime: String?,
    val addTopPadding: Boolean,
    private val daysAgo: RelativeTimestamp,
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val dateFormatter: DateTimeFormatter,
    val isBpEditable: Boolean
) : GroupieItemWithUiEvents<SummaryBloodPressureListItem.BpViewHolder>(measurement.uuid.hashCode().toLong()) {

  companion object {
    fun from(
        bloodPressures: List<BloodPressureMeasurement>,
        timestampGenerator: RelativeTimestampGenerator,
        dateFormatter: DateTimeFormatter,
        canEditFor: Duration,
        bpTimeFormatter: DateTimeFormatter,
        zoneId: ZoneId,
        utcClock: UtcClock,
        userClock: UserClock
    ): List<SummaryBloodPressureListItem> {
      val measurementsByDate = bloodPressures.groupBy { item -> item.recordedAt.atZone(utcClock.zone).toLocalDate() }

      return measurementsByDate.mapValues { (_, measurementList) ->
        measurementList.map { measurement ->
          val timestamp = timestampGenerator.generate(measurement.recordedAt, userClock)
          SummaryBloodPressureListItem(
              measurement = measurement,
              showDivider = measurement == measurementList.last(),
              formattedTime = if (measurementList.size > 1) displayTime(measurement.recordedAt, zoneId, bpTimeFormatter) else null,
              addTopPadding = measurement == measurementList.first(),
              daysAgo = timestamp,
              dateFormatter = dateFormatter,
              isBpEditable = isBpEditable(measurement, canEditFor, utcClock)
          )
        }
      }.values.flatten()
    }

    private fun displayTime(
        instant: Instant,
        zoneId: ZoneId,
        formatter: DateTimeFormatter
    ): String = instant.atZone(zoneId).format(formatter)

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

  override lateinit var uiEvents: Subject<UiEvent>

  override fun getLayout() = R.layout.list_patientsummary_bp_measurement

  override fun createViewHolder(itemView: View): BpViewHolder {
    return BpViewHolder(itemView)
  }

  override fun bind(holder: BpViewHolder, position: Int) {
    (holder.itemView as BloodPressureItemView).render(
        isBpEditable = isBpEditable,
        uiEvents = uiEvents,
        measurement = measurement,
        daysAgo = daysAgo,
        showDivider = showDivider,
        formattedTime = formattedTime,
        dateFormatter = dateFormatter,
        addTopPadding = addTopPadding
    )
  }

  override fun isSameAs(other: Item<*>?): Boolean {
    return this == other
  }

  class BpViewHolder(override val containerView: View) : ViewHolder(containerView), LayoutContainer
}
