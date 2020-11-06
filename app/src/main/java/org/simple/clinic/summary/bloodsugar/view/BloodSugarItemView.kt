package org.simple.clinic.summary.bloodsugar.view

import android.annotation.SuppressLint
import android.content.Context
import android.text.style.TextAppearanceSpan
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import kotlinx.android.synthetic.main.patientsummary_bloodsugar_item_content.view.*
import org.simple.clinic.R
import org.simple.clinic.bloodsugar.BloodSugarMeasurement
import org.simple.clinic.bloodsugar.BloodSugarReading
import org.simple.clinic.bloodsugar.BloodSugarUnitPreference
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
      bloodSugarUnitPreference: BloodSugarUnitPreference,
      editMeasurementClicked: (BloodSugarMeasurement) -> Unit
  ) {
    renderBloodSugarReading(measurement.reading, bloodSugarUnitPreference)
    renderBloodSugarLevel(measurement.reading)
    renderDateTime(bloodSugarDate, bloodSugarTime)

    bloodSugarItemRoot.apply {
      setOnClickListener { editMeasurementClicked(measurement) }
      isClickable = isBloodSugarEditable
      isFocusable = isBloodSugarEditable
    }
    bloodSugarEditButton.visibleOrGone(isBloodSugarEditable)
  }

  private fun renderBloodSugarLevel(reading: BloodSugarReading) {
    when {
      reading.isLow -> {
        bloodSugarLevelTextView.visibility = View.VISIBLE
        bloodSugarLevelTextView.text = context.getString(R.string.bloodsugar_level_low)
        bloodSugarIconImageView.setImageResource(R.drawable.ic_blood_sugar_filled)
      }
      reading.isHigh -> {
        bloodSugarLevelTextView.visibility = View.VISIBLE
        bloodSugarLevelTextView.text = context.getString(R.string.bloodsugar_level_high)
        bloodSugarIconImageView.setImageResource(R.drawable.ic_blood_sugar_filled)
      }
      else -> {
        bloodSugarLevelTextView.visibility = View.GONE
        bloodSugarIconImageView.setImageResource(R.drawable.ic_blood_sugar_outline)
      }
    }
  }

  @SuppressLint("SetTextI18n")
  private fun renderBloodSugarReading(reading: BloodSugarReading, bloodSugarUnitPreference: BloodSugarUnitPreference) {
    val displayUnit = context.getString(reading.displayUnit(bloodSugarUnitPreference))
    val displayType = context.getString(reading.displayType)
    val readingPrefix = reading.displayValue(bloodSugarUnitPreference)
    val readingSuffix = "$displayUnit $displayType"

    readingTextView.text = "$readingPrefix${reading.displayUnitSeparator}$readingSuffix"
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
}
