package org.simple.clinic.summary.bloodpressures

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.cardview.widget.CardView
import kotlinx.android.synthetic.main.patientsummary_bpsummary_content.view.*
import org.simple.clinic.R
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.util.RelativeTimestampGenerator
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.UtcClock
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter

private typealias EditMeasurementClicked = (BloodPressureMeasurement) -> Unit
private typealias NewBpClicked = () -> Unit

class BloodPressureSummaryView(
    context: Context,
    attributeSet: AttributeSet
) : CardView(context, attributeSet) {

  init {
    LayoutInflater.from(context).inflate(R.layout.patientsummary_bpsummary_content, this, true)
  }

  var editMeasurementClicked: EditMeasurementClicked? = null
  var newBpClicked: NewBpClicked? = null

  fun render(
      bloodPressureMeasurements: List<BloodPressureMeasurement>,
      utcClock: UtcClock,
      placeholderLimit: Int,
      timestampGenerator: RelativeTimestampGenerator,
      dateFormatter: DateTimeFormatter,
      canEditFor: Duration,
      bpTimeFormatter: DateTimeFormatter,
      zoneId: ZoneId,
      userClock: UserClock
  ) {
    newBp.setOnClickListener { newBpClicked?.invoke() }

    val placeholderViews = generatePlaceholders(bloodPressureMeasurements, utcClock, placeholderLimit)
    val listItemViews = generateBpViews(bloodPressureMeasurements, timestampGenerator, userClock, zoneId, bpTimeFormatter, dateFormatter, canEditFor, utcClock)

    bpItemContainer.removeAllViews()
    withDividers(listItemViews + placeholderViews).forEach(bpItemContainer::addView)
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
      utcClock: UtcClock
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
            isBpEditable = isBpEditable(measurement, canEditFor, utcClock)
        ) { clickedMeasurement -> editMeasurementClicked?.invoke(clickedMeasurement) }

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

  private fun inflateDividerView() = LayoutInflater.from(context).inflate(R.layout.patientsummary_bpsummary_divider, this, false)

  private fun withDividers(views: List<View>): List<View> {
    return views
        .mapIndexed { index: Int, view: View ->
          val isLastViewInList = index == views.lastIndex

          if (isLastViewInList) listOf(view) else listOf(view, inflateDividerView())
        }
        .flatten()
  }
}
