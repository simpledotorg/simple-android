package org.simple.clinic.summary.bloodsugar.view

import android.annotation.SuppressLint
import android.content.Context
import android.text.style.TextAppearanceSpan
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import kotlinx.android.synthetic.main.patientsummary_bloodsugar_item_content.view.*
import org.simple.clinic.R
import org.simple.clinic.bloodsugar.BloodSugarMeasurement
import org.simple.clinic.bloodsugar.BloodSugarMeasurementType
import org.simple.clinic.bloodsugar.BloodSugarReading
import org.simple.clinic.bloodsugar.Fasting
import org.simple.clinic.bloodsugar.HbA1c
import org.simple.clinic.bloodsugar.PostPrandial
import org.simple.clinic.bloodsugar.Random
import org.simple.clinic.bloodsugar.Unknown
import org.simple.clinic.util.Truss
import org.simple.clinic.widgets.visibleOrGone

class BloodSugarItemView(
    context: Context,
    attributeSet: AttributeSet
) : FrameLayout(context, attributeSet) {

  init {
    LayoutInflater.from(context).inflate(R.layout.patientsummary_bloodsugar_item_content, this, true)
  }

  fun render(
      measurement: BloodSugarMeasurement,
      bloodSugarDate: String,
      bloodSugarTime: String?,
      isBloodSugarEditable: Boolean,
      editMeasurementClicked: (BloodSugarMeasurement) -> Unit
  ) {
    renderBloodSugarReading(measurement.reading)
    renderDateTime(bloodSugarDate, bloodSugarTime)

    bloodSugarItemRoot.apply {
      setOnClickListener { editMeasurementClicked(measurement) }
      isClickable = isBloodSugarEditable
      isFocusable = isBloodSugarEditable
    }
    bloodSugarEditButton.visibleOrGone(isBloodSugarEditable)
  }

  @SuppressLint("SetTextI18n")
  private fun renderBloodSugarReading(reading: BloodSugarReading) {
    val readingPrefix = reading.displayValue
    val readingSuffix = "${unitForReadingType(context, reading.type)} ${textForReadingType(context, reading.type)}"

    readingTextView.text = "$readingPrefix${reading.displayUnitSeparator}$readingSuffix"

    if (reading.isHigh) {
      bloodSugarIconImageView.setImageResource(R.drawable.ic_blood_sugar_filled)
    } else {
      bloodSugarIconImageView.setImageResource(R.drawable.ic_blood_sugar_outline)
    }
  }

  private fun renderDateTime(bloodSugarDate: String, bloodSugarTime: String?) {
    val dateTimeTextAppearanceSpan = if (bloodSugarTime != null) {
      TextAppearanceSpan(context, R.style.Clinic_V2_TextAppearance_Caption_Grey1)
    } else {
      TextAppearanceSpan(context, R.style.Clinic_V2_TextAppearance_Body2Left_Grey1)
    }
    val bloodSugarDateTime = if (bloodSugarTime != null) {
      context.getString(R.string.bloodpressurehistory_blood_sugar_date_time, bloodSugarDate, bloodSugarTime)
    } else {
      bloodSugarDate
    }

    dateTimeTextView.text = Truss()
        .pushSpan(dateTimeTextAppearanceSpan)
        .append(bloodSugarDateTime)
        .popSpan()
        .build()
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
