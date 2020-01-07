package org.simple.clinic.summary.bloodsugar.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.cardview.widget.CardView
import kotlinx.android.synthetic.main.patientsummary_bloodsugarsummary_content.view.*
import org.simple.clinic.R
import org.simple.clinic.bloodsugar.BloodSugarMeasurement
import org.simple.clinic.util.RelativeTimestampGenerator
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.UtcClock
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter

private typealias AddNewBloodSugarClicked = () -> Unit

class BloodSugarSummaryView(
    context: Context,
    attributes: AttributeSet
) : CardView(context, attributes) {

  init {
    LayoutInflater.from(context).inflate(R.layout.patientsummary_bloodsugarsummary_content, this, true)
  }

  var addNewBloodSugarClicked: AddNewBloodSugarClicked? = null

  fun render(
      bloodSugarMeasurements: List<BloodSugarMeasurement>,
      utcClock: UtcClock,
      placeholderLimit: Int,
      timestampGenerator: RelativeTimestampGenerator,
      dateFormatter: DateTimeFormatter,
      bpTimeFormatter: DateTimeFormatter,
      zoneId: ZoneId,
      userClock: UserClock
  ) {
    newBloodSugar.setOnClickListener { addNewBloodSugarClicked?.invoke() }

    val placeholderViews = generatePlaceholders(bloodSugarMeasurements, utcClock, placeholderLimit)
    val listItemViews = generateBloodSugarRows(bloodSugarMeasurements, timestampGenerator, userClock, zoneId, bpTimeFormatter, dateFormatter, utcClock)

    bloodSugarItemContainer.removeAllViews()
    withDividers(listItemViews + placeholderViews).forEach(bloodSugarItemContainer::addView)
  }

  private fun generatePlaceholders(
      bloodSugarMeasurements: List<BloodSugarMeasurement>,
      utcClock: UtcClock,
      placeholderLimit: Int
  ): List<View> {
    val measurementsByDate = bloodSugarMeasurements.groupBy { item -> item.recordedAt.atZone(utcClock.zone).toLocalDate() }
    val numberOfBloodSugarGroups = measurementsByDate.size

    val numberOfPlaceholders = 0.coerceAtLeast(placeholderLimit - numberOfBloodSugarGroups)

    return (1..numberOfPlaceholders).map { placeholderNumber ->
      val shouldShowHint = numberOfBloodSugarGroups == 0 && placeholderNumber == 1

      val placeholderItemView = LayoutInflater.from(context).inflate(R.layout.list_patientsummary_bloodsugar_placeholder, this, false) as BloodSugarPlaceholderItemView
      placeholderItemView.render(showHint = shouldShowHint)

      placeholderItemView
    }
  }

  private fun generateBloodSugarRows(
      bloodSugarMeasurements: List<BloodSugarMeasurement>,
      timestampGenerator: RelativeTimestampGenerator,
      userClock: UserClock,
      zoneId: ZoneId,
      bpTimeFormatter: DateTimeFormatter,
      dateFormatter: DateTimeFormatter,
      utcClock: UtcClock
  ): List<View> {
    val measurementsByDate = bloodSugarMeasurements.groupBy { item -> item.recordedAt.atZone(utcClock.zone).toLocalDate() }

    return measurementsByDate.mapValues { (_, measurementList) ->
      measurementList.map { measurement ->
        val timestamp = timestampGenerator.generate(measurement.recordedAt, userClock)

        val bloodSugarItemView = LayoutInflater.from(context).inflate(R.layout.list_patientsummary_bloodsugar_measurement, this, false) as BloodSugarItemView
        bloodSugarItemView.render(
            measurement = measurement,
            formattedTime = if (measurementList.size > 1) displayTime(measurement.recordedAt, zoneId, bpTimeFormatter) else null,
            addTopPadding = measurement == measurementList.first(),
            daysAgo = timestamp,
            dateFormatter = dateFormatter
        )

        bloodSugarItemView
      }
    }.values.flatten()
  }

  private fun displayTime(
      instant: Instant,
      zoneId: ZoneId,
      formatter: DateTimeFormatter
  ): String = instant.atZone(zoneId).format(formatter)

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
