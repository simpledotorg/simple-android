package org.simple.clinic.summary.bloodsugar.view

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import kotlinx.android.synthetic.main.patientsummary_bloodsugarsummary_content.view.*
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.bloodsugar.BloodSugarMeasurement
import org.simple.clinic.bloodsugar.selection.type.BloodSugarTypePickerSheet
import org.simple.clinic.summary.PatientSummaryConfig
import org.simple.clinic.summary.bloodsugar.BloodSugarSummaryViewEvent
import org.simple.clinic.summary.bloodsugar.BloodSugarSummaryViewUi
import org.simple.clinic.summary.bloodsugar.NewBloodSugarClicked
import org.simple.clinic.summary.bloodsugar.UiActions
import org.simple.clinic.util.RelativeTimestampGenerator
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.unsafeLazy
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Named

class BloodSugarSummaryView(
    context: Context,
    attributes: AttributeSet
) : CardView(context, attributes), BloodSugarSummaryViewUi, UiActions {

  @Inject
  lateinit var activity: AppCompatActivity

  @Inject
  lateinit var userClock: UserClock

  @field:[Inject Named("exact_date")]
  lateinit var exactDateFormatter: DateTimeFormatter

  @Inject
  lateinit var timestampGenerator: RelativeTimestampGenerator

  @Inject
  lateinit var config: PatientSummaryConfig

  @Inject
  lateinit var utcClock: UtcClock

  @Inject
  lateinit var zoneId: ZoneId

  @field:[Inject Named("time_for_bps_recorded")]
  lateinit var timeFormatter: DateTimeFormatter

  init {
    LayoutInflater.from(context).inflate(R.layout.patientsummary_bloodsugarsummary_content, this, true)
  }

  private val events: Observable<BloodSugarSummaryViewEvent> by unsafeLazy {
    addNewBloodSugarClicks()
        .compose(ReportAnalyticsEvents())
        .cast<BloodSugarSummaryViewEvent>()
  }

  private fun addNewBloodSugarClicks(): Observable<BloodSugarSummaryViewEvent> {
    return newBloodSugar.clicks().map { NewBloodSugarClicked }
  }

  override fun showBloodSugarSummary(bloodSugars: List<BloodSugarMeasurement>) {
    render(bloodSugars)
  }

  override fun showNoBloodSugarsView() {
    render(emptyList())
  }

  override fun showBloodSugarTypeSelector() {
    activity.startActivity(Intent(context, BloodSugarTypePickerSheet::class.java))
  }

  private fun render(bloodSugarMeasurements: List<BloodSugarMeasurement>) {
    val placeholderViews = generatePlaceholders(bloodSugarMeasurements)
    val listItemViews = generateBloodSugarRows(bloodSugarMeasurements)

    bloodSugarItemContainer.removeAllViews()
    withDividers(listItemViews + placeholderViews).forEach(bloodSugarItemContainer::addView)
  }

  private fun generatePlaceholders(bloodSugarMeasurements: List<BloodSugarMeasurement>): List<View> {
    val measurementsByDate = bloodSugarMeasurements.groupBy { item -> item.recordedAt.atZone(utcClock.zone).toLocalDate() }
    val numberOfBloodSugarGroups = measurementsByDate.size

    val numberOfPlaceholders = 0.coerceAtLeast(config.numberOfBpPlaceholders - numberOfBloodSugarGroups)

    return (1..numberOfPlaceholders).map { placeholderNumber ->
      val shouldShowHint = numberOfBloodSugarGroups == 0 && placeholderNumber == 1

      val placeholderItemView = LayoutInflater.from(context).inflate(R.layout.list_patientsummary_bloodsugar_placeholder, this, false) as BloodSugarPlaceholderItemView
      placeholderItemView.render(showHint = shouldShowHint)

      placeholderItemView
    }
  }

  private fun generateBloodSugarRows(bloodSugarMeasurements: List<BloodSugarMeasurement>): List<View> {
    val measurementsByDate = bloodSugarMeasurements.groupBy { item -> item.recordedAt.atZone(utcClock.zone).toLocalDate() }

    return measurementsByDate.mapValues { (_, measurementList) ->
      measurementList.map { measurement ->
        val timestamp = timestampGenerator.generate(measurement.recordedAt, userClock)

        val bloodSugarItemView = LayoutInflater.from(context).inflate(R.layout.list_patientsummary_bloodsugar_measurement, this, false) as BloodSugarItemView
        bloodSugarItemView.render(
            measurement = measurement,
            formattedTime = if (measurementList.size > 1) displayTime(measurement.recordedAt, zoneId, timeFormatter) else null,
            addTopPadding = measurement == measurementList.first(),
            daysAgo = timestamp,
            dateFormatter = exactDateFormatter
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
