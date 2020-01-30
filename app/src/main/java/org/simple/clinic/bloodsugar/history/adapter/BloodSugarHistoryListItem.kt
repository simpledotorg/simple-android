package org.simple.clinic.bloodsugar.history.adapter

import android.content.Context
import android.text.style.TextAppearanceSpan
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.list_blood_sugar_history_item.*
import kotlinx.android.synthetic.main.list_new_blood_sugar_button.*
import org.simple.clinic.R
import org.simple.clinic.bloodsugar.BloodSugarMeasurement
import org.simple.clinic.bloodsugar.BloodSugarMeasurementType
import org.simple.clinic.bloodsugar.Fasting
import org.simple.clinic.bloodsugar.PostPrandial
import org.simple.clinic.bloodsugar.Random
import org.simple.clinic.bloodsugar.Unknown
import org.simple.clinic.util.Truss
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.toLocalDateAtZone
import org.simple.clinic.widgets.ItemAdapter
import org.simple.clinic.widgets.recyclerview.ViewHolderX
import org.threeten.bp.format.DateTimeFormatter

sealed class BloodSugarHistoryListItem : ItemAdapter.Item<Event> {
  companion object {
    fun from(
        measurements: List<BloodSugarMeasurement>,
        userClock: UserClock,
        dateFormatter: DateTimeFormatter,
        timeFormatter: DateTimeFormatter
    ): List<BloodSugarHistoryListItem> {
      val measurementsByDate = measurements.groupBy { it.recordedAt.toLocalDateAtZone(userClock.zone) }

      val bloodSugarHistoryItems = measurementsByDate.mapValues { (_, measurementsList) ->
        val hasMultipleMeasurementsInSameDate = measurementsList.size > 1
        measurementsList.map { measurement ->
          val recordedAt = measurement.recordedAt.toLocalDateAtZone(userClock.zone)
          val bloodSugarTime = if (hasMultipleMeasurementsInSameDate) {
            timeFormatter.format(measurement.recordedAt.atZone(userClock.zone))
          } else {
            null
          }

          BloodSugarHistoryItem(
              measurement = measurement,
              bloodSugarDate = dateFormatter.format(recordedAt),
              bloodSugarTime = bloodSugarTime
          )
        }
      }.values.flatten()

      return listOf(NewBloodSugarButton) + bloodSugarHistoryItems
    }
  }

  object NewBloodSugarButton : BloodSugarHistoryListItem() {
    override fun layoutResId(): Int = R.layout.list_new_blood_sugar_button

    override fun render(holder: ViewHolderX, subject: Subject<Event>) {
      holder.newBloodSugar.setOnClickListener { subject.onNext(NewBloodSugarClicked) }
    }
  }

  data class BloodSugarHistoryItem(
      val measurement: BloodSugarMeasurement,
      val bloodSugarDate: String,
      val bloodSugarTime: String?
  ) : BloodSugarHistoryListItem() {
    override fun layoutResId(): Int = R.layout.list_blood_sugar_history_item

    override fun render(holder: ViewHolderX, subject: Subject<Event>) {
      val context = holder.itemView.context
      val bloodSugarReading = measurement.reading
      val bloodSugarDateTime = if (bloodSugarTime != null) {
        context.getString(R.string.bloodsugarhistory_blood_sugar_date_time, bloodSugarDate, bloodSugarTime)
      } else {
        bloodSugarDate
      }
      val dateTimeTextAppearance = if (bloodSugarTime != null) {
        TextAppearanceSpan(context, R.style.Clinic_V2_TextAppearance_Caption_Grey1)
      } else {
        TextAppearanceSpan(context, R.style.Clinic_V2_TextAppearance_Body2Left_Grey1)
      }
      val formattedBPDateTime = Truss()
          .pushSpan(dateTimeTextAppearance)
          .append(bloodSugarDateTime)
          .popSpan()
          .build()

      holder.readingsTextView.text = context.getString(R.string.bloodsugarhistory_blood_sugar_reading, bloodSugarReading.value, textForReadingType(context, bloodSugarReading.type))
      holder.dateTimeTextView.text = formattedBPDateTime
    }

    private fun textForReadingType(context: Context, type: BloodSugarMeasurementType): String {
      return when (type) {
        Random -> context.getString(R.string.bloodsugarsummary_bloodsugartype_rbs)
        PostPrandial -> context.getString(R.string.bloodsugarsummary_bloodsugartype_ppbs)
        Fasting -> context.getString(R.string.bloodsugarsummary_bloodsugartype_fbs)
        is Unknown -> ""
      }
    }
  }
}
