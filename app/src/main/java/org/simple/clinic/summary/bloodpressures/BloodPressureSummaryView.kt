package org.simple.clinic.summary.bloodpressures

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import org.simple.clinic.R
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.util.RelativeTimestampGenerator
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.UtcClock
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter

class BloodPressureSummaryView(
    context: Context,
    attributeSet: AttributeSet
) : LinearLayout(context, attributeSet) {

  init {
    orientation = VERTICAL
  }

  fun render(
      bloodPressureMeasurements: List<BloodPressureMeasurement>,
      utcClock: UtcClock,
      placeholderLimit: Int,
      timestampGenerator: RelativeTimestampGenerator,
      dateFormatter: DateTimeFormatter,
      canEditFor: Duration,
      bpTimeFormatter: DateTimeFormatter,
      zoneId: ZoneId,
      userClock: UserClock,
      editMeasurementClicked: (BloodPressureMeasurement) -> Unit
  ) {
    val placeholderViews = generatePlaceholders(bloodPressureMeasurements, utcClock, placeholderLimit)
    val listItemViews = generateBpViews(bloodPressureMeasurements, timestampGenerator, userClock, zoneId, bpTimeFormatter, dateFormatter, canEditFor, utcClock, editMeasurementClicked)

    removeAllViews()
    (listItemViews + placeholderViews).forEach(::addView)
  }

  private fun generatePlaceholders(
      bloodPressureMeasurements: List<BloodPressureMeasurement>,
      utcClock: UtcClock,
      placeholderLimit: Int
  ): List<View> {
    val measurementsByDate = bloodPressureMeasurements.groupBy { item -> item.recordedAt.atZone(utcClock.zone).toLocalDate() }
    val numberOfBloodPressureGroups = measurementsByDate.size

    val numberOfPlaceholders = 0.coerceAtLeast(placeholderLimit - numberOfBloodPressureGroups)

    return (1..numberOfPlaceholders).map { placeholderNumber ->
      val shouldShowHint = numberOfBloodPressureGroups == 0 && placeholderNumber == 1
      val shouldShowDivider = placeholderNumber != numberOfPlaceholders

      val placeholderItemView = LayoutInflater.from(context).inflate(R.layout.list_patientsummary_bp_placeholder, this, false) as BloodPressurePlaceholderItemView
      placeholderItemView.render(showHint = shouldShowHint, showDivider = shouldShowDivider)

      placeholderItemView
    }
  }

  private fun generateBpViews(
      bloodPressureMeasurements: List<BloodPressureMeasurement>,
      timestampGenerator: RelativeTimestampGenerator,
      userClock: UserClock,
      zoneId: ZoneId,
      bpTimeFormatter: DateTimeFormatter,
      dateFormatter: DateTimeFormatter,
      canEditFor: Duration,
      utcClock: UtcClock,
      editMeasurementClicked: (BloodPressureMeasurement) -> Unit
  ): List<View> {
    val measurementsByDate = bloodPressureMeasurements.groupBy { item -> item.recordedAt.atZone(utcClock.zone).toLocalDate() }

    return measurementsByDate.mapValues { (_, measurementList) ->
      measurementList.map { measurement ->
        val timestamp = timestampGenerator.generate(measurement.recordedAt, userClock)

        val bloodPressureItemView = LayoutInflater.from(context).inflate(R.layout.list_patientsummary_bp_measurement, this, false) as BloodPressureItemView
        bloodPressureItemView.render(
            measurement = measurement,
            showDivider = measurement == measurementList.last(),
            formattedTime = if (measurementList.size > 1) displayTime(measurement.recordedAt, zoneId, bpTimeFormatter) else null,
            addTopPadding = measurement == measurementList.first(),
            daysAgo = timestamp,
            dateFormatter = dateFormatter,
            isBpEditable = isBpEditable(measurement, canEditFor, utcClock),
            editMeasurementClicked = editMeasurementClicked
        )

        bloodPressureItemView
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
