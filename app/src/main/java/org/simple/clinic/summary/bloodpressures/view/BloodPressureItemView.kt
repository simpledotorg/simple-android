package org.simple.clinic.summary.bloodpressures.view

import android.annotation.SuppressLint
import android.content.Context
import android.text.style.TextAppearanceSpan
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import org.simple.clinic.R
import org.simple.clinic.bp.BloodPressureLevel
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.databinding.PatientsummaryBpitemContentBinding
import org.simple.clinic.util.Truss
import org.simple.clinic.widgets.visibleOrGone

class BloodPressureItemView(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

  private var binding: PatientsummaryBpitemContentBinding? = null

  private val bpItemRoot
    get() = binding!!.bpItemRoot

  private val editButton
    get() = binding!!.editButton

  private val readingsTextView
    get() = binding!!.readingsTextView

  private val bpHighTextView
    get() = binding!!.bpHighTextView

  private val heartImageView
    get() = binding!!.heartImageView

  private val dateTimeTextView
    get() = binding!!.dateTimeTextView

  init {
    val layoutInflater = LayoutInflater.from(context)
    binding = PatientsummaryBpitemContentBinding.inflate(layoutInflater, this, true)
  }

  fun render(
      measurement: BloodPressureMeasurement,
      isBpEditable: Boolean,
      bpDate: String,
      bpTime: String?,
      editMeasurementClicked: (BloodPressureMeasurement) -> Unit
  ) {
    renderBloodPressureReading(measurement.reading.systolic, measurement.reading.diastolic, measurement.level)
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
      bpLevel: BloodPressureLevel
  ) {
    val isBpHigh = bpLevel.isHigh

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

  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()
    binding = null
  }
}
