package org.simple.clinic.bloodsugar.history.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.text.style.TextAppearanceSpan
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.list_blood_sugar_history_item.*
import kotlinx.android.synthetic.main.list_new_blood_sugar_button.*
import org.simple.clinic.R
import org.simple.clinic.bloodsugar.BloodSugarMeasurement
import org.simple.clinic.bloodsugar.BloodSugarMeasurementType
import org.simple.clinic.bloodsugar.Fasting
import org.simple.clinic.bloodsugar.HbA1c
import org.simple.clinic.bloodsugar.PostPrandial
import org.simple.clinic.bloodsugar.Random
import org.simple.clinic.bloodsugar.Unknown
import org.simple.clinic.util.Truss
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.toLocalDateAtZone
import org.simple.clinic.widgets.PagingItemAdapter
import org.simple.clinic.widgets.recyclerview.ViewHolderX
import org.simple.clinic.widgets.visibleOrGone
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import org.threeten.bp.format.DateTimeFormatter

sealed class BloodSugarHistoryListItem : PagingItemAdapter.Item<Event> {
  companion object {
    fun from(
        measurements: List<BloodSugarMeasurement>,
        userClock: UserClock,
        dateFormatter: DateTimeFormatter,
        timeFormatter: DateTimeFormatter,
        canEditFor: Duration,
        utcClock: UtcClock
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
          val isBloodSugarEditable = isBloodSugarEditable(measurement, canEditFor, utcClock)

          BloodSugarHistoryItem(
              measurement = measurement,
              bloodSugarDate = dateFormatter.format(recordedAt),
              bloodSugarTime = bloodSugarTime,
              isBloodSugarEditable = isBloodSugarEditable
          )
        }
      }.values.flatten()

      return listOf(NewBloodSugarButton) + bloodSugarHistoryItems
    }

    private fun isBloodSugarEditable(
        bloodSugarMeasurement: BloodSugarMeasurement,
        bpEditableFor: Duration,
        utcClock: UtcClock
    ): Boolean {
      return if (bloodSugarMeasurement.reading.type is Unknown) {
        false
      } else {
        val now = Instant.now(utcClock)
        val createdAt = bloodSugarMeasurement.timestamps.createdAt

        val durationSinceBpCreated = Duration.between(createdAt, now)

        durationSinceBpCreated <= bpEditableFor
      }
    }
  }

  object NewBloodSugarButton : BloodSugarHistoryListItem() {
    override fun layoutResId(): Int = R.layout.list_new_blood_sugar_button

    override fun render(holder: ViewHolderX, subject: Subject<Event>) {
      holder.newBloodSugarButton.setOnClickListener { subject.onNext(NewBloodSugarClicked) }
    }
  }

  data class BloodSugarHistoryItem(
      val measurement: BloodSugarMeasurement,
      val bloodSugarDate: String,
      val bloodSugarTime: String?,
      val isBloodSugarEditable: Boolean
  ) : BloodSugarHistoryListItem() {
    override fun layoutResId(): Int = R.layout.list_blood_sugar_history_item

    @SuppressLint("SetTextI18n")
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
      val isBloodSugarHigh = measurement.reading.isHigh

      val readingPrefix = bloodSugarReading.displayValue
      val readingSuffix = "${unitForReadingType(context, bloodSugarReading.type)} ${textForReadingType(context, bloodSugarReading.type)}"

      holder.readingsTextView.text = "$readingPrefix${bloodSugarReading.displayUnitSeparator}$readingSuffix"
      holder.dateTimeTextView.text = formattedBPDateTime
      if (isBloodSugarHigh) {
        holder.bloodSugarIconImageView.setImageResource(R.drawable.ic_blood_sugar_filled)
      } else {
        holder.bloodSugarIconImageView.setImageResource(R.drawable.ic_blood_sugar_outline)
      }

      if (isBloodSugarEditable) {
        holder.itemView.setOnClickListener { subject.onNext(BloodSugarHistoryItemClicked(measurement)) }
      } else {
        holder.itemView.setOnClickListener(null)
      }
      holder.itemView.isClickable = isBloodSugarEditable
      holder.itemView.isFocusable = isBloodSugarEditable
      holder.editButton.visibleOrGone(isBloodSugarEditable)
    }

    private fun unitForReadingType(context: Context, type: BloodSugarMeasurementType): String {
      return when (type) {
        Random, PostPrandial, Fasting -> context.getString(R.string.bloodsugarhistory_unit_type_mg_dl)
        HbA1c -> context.getString(R.string.bloodsugarhistory_unit_type_percentage)
        is Unknown -> ""
      }
    }

    private fun textForReadingType(context: Context, type: BloodSugarMeasurementType): String {
      return when (type) {
        Random -> context.getString(R.string.bloodsugarsummary_bloodsugartype_rbs)
        PostPrandial -> context.getString(R.string.bloodsugarsummary_bloodsugartype_ppbs)
        Fasting -> context.getString(R.string.bloodsugarsummary_bloodsugartype_fbs)
        HbA1c -> context.getString(R.string.bloodsugarsummary_bloodsugartype_hba1c)
        is Unknown -> ""
      }
    }
  }
}
