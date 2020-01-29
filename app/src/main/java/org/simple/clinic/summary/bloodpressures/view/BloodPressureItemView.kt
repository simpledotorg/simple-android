package org.simple.clinic.summary.bloodpressures.view

import android.annotation.SuppressLint
import android.content.Context
import android.text.style.TextAppearanceSpan
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import kotlinx.android.synthetic.main.patientsummary_bpitem_content.view.*
import org.simple.clinic.R
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.util.Truss
import org.simple.clinic.widgets.visibleOrGone

class BloodPressureItemView(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

  init {
    LayoutInflater.from(context).inflate(R.layout.patientsummary_bpitem_content, this, true)
  }

  fun render(
      measurement: BloodPressureMeasurement,
      isBpEditable: Boolean,
      bpDate: String,
      bpTime: String?,
      editMeasurementClicked: (BloodPressureMeasurement) -> Unit
  ) {
    renderBloodPressureReading(measurement.systolic, measurement.diastolic, measurement.level.isUrgent())
    renderDateTime(bpDate, bpTime)

    if (isBpEditable) bpItemRoot.setOnClickListener { editMeasurementClicked(measurement) }
    bpItemRoot.isClickable = isBpEditable
    bpItemRoot.isFocusable = isBpEditable
    editButton.visibleOrGone(isBpEditable)
  }

  @SuppressLint("SetTextI18n")
  private fun renderBloodPressureReading(
      systolic: Int,
      diastolic: Int,
      isBpHigh: Boolean
  ) {
    readingsTextView.text = "$systolic / $diastolic"
    bpHighTextView.visibleOrGone(isBpHigh)
    if (isBpHigh) {
      heartImageView.setImageResource(R.drawable.bp_reading_high)
    } else {
      heartImageView.setImageResource(R.drawable.bp_reading_normal)
    }
  }

  private fun renderDateTime(bpDate: String, bpTime: String?) {
    val dateTimeTextAppearanceSpan = if (bpTime != null) {
      TextAppearanceSpan(context, R.style.Clinic_V2_TextAppearance_Caption_Grey1)
    } else {
      TextAppearanceSpan(context, R.style.Clinic_V2_TextAppearance_Body2Left_Grey1)
    }
    val bpDateTime = if (bpTime != null) {
      context.getString(R.string.patientsummary_newbp_date_time, bpDate, bpTime)
    } else {
      bpDate
    }
    val dateTimeFormattedString = Truss()
        .pushSpan(dateTimeTextAppearanceSpan)
        .append(bpDateTime)
        .popSpan()
        .build()

    dateTimeTextView.text = dateTimeFormattedString
  }
}
