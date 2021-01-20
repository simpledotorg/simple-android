package org.simple.clinic.summary.bloodsugar.view

import android.annotation.SuppressLint
import android.content.Context
import android.text.style.TextAppearanceSpan
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import org.simple.clinic.R
import org.simple.clinic.bloodsugar.BloodSugarMeasurement
import org.simple.clinic.bloodsugar.BloodSugarReading
import org.simple.clinic.bloodsugar.BloodSugarUnitPreference
import org.simple.clinic.databinding.PatientsummaryBloodsugarItemContentBinding
import org.simple.clinic.util.Truss
import org.simple.clinic.widgets.visibleOrGone

class BloodSugarItemView(
    context: Context,
    attributeSet: AttributeSet
) : FrameLayout(context, attributeSet) {

  private var binding: PatientsummaryBloodsugarItemContentBinding? = null

  private val bloodSugarItemRoot
    get() = binding!!.bloodSugarItemRoot

  private val bloodSugarEditButton
    get() = binding!!.bloodSugarEditButton

  private val bloodSugarLevelTextView
    get() = binding!!.bloodSugarLevelTextView

  private val bloodSugarIconImageView
    get() = binding!!.bloodSugarIconImageView

  private val readingTextView
    get() = binding!!.readingTextView

  private val dateTimeTextView
    get() = binding!!.dateTimeTextView

  init {
    val layoutInflater = LayoutInflater.from(context)
    binding = PatientsummaryBloodsugarItemContentBinding.inflate(layoutInflater, this, true)
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
      TextAppearanceSpan(context, R.style.TextAppearance_Simple_Caption)
    } else {
      TextAppearanceSpan(context, R.style.TextAppearance_Simple_Body2)
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

  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()
    binding = null
  }
}
