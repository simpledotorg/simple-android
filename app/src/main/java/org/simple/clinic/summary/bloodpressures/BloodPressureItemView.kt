package org.simple.clinic.summary.bloodpressures

import android.content.Context
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.res.ResourcesCompat
import kotlinx.android.synthetic.main.patientsummary_bpitem_content.view.*
import org.simple.clinic.R
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.util.Just
import org.simple.clinic.util.None
import org.simple.clinic.util.RelativeTimestamp
import org.simple.clinic.util.Truss
import org.simple.clinic.util.Unicode
import org.simple.clinic.widgets.setPaddingBottom
import org.simple.clinic.widgets.setPaddingTop
import org.simple.clinic.widgets.setTextAppearanceCompat
import org.threeten.bp.format.DateTimeFormatter

class BloodPressureItemView(
    context: Context,
    attributeSet: AttributeSet
) : FrameLayout(context, attributeSet) {

  init {
    LayoutInflater.from(context).inflate(R.layout.patientsummary_bpitem_content, this, true)
  }

  fun render(
      isBpEditable: Boolean,
      measurement: BloodPressureMeasurement,
      daysAgo: RelativeTimestamp,
      showDivider: Boolean,
      formattedTime: String?,
      dateFormatter: DateTimeFormatter,
      addTopPadding: Boolean,
      editMeasurementClicked: (BloodPressureMeasurement) -> Unit
  ) {
    isClickable = isBpEditable
    isFocusable = isBpEditable
    if (isBpEditable) setOnClickListener { editMeasurementClicked(measurement) }

    renderBpReading(measurement)
    renderBpLevel(measurement)
    renderRelativeTimestampWithEditButton(daysAgo, dateFormatter, isBpEditable)
    setIconTint(measurement)
    renderDivider(showDivider)
    renderTimeOfDay(formattedTime)
    addPadding(formattedTime, addTopPadding, showDivider)
  }

  private fun addPadding(formattedTime: String?, addTopPadding: Boolean, showDivider: Boolean) {
    val multipleItemsInThisGroup = formattedTime != null
    addTopPadding(itemLayout = itemLayout, multipleItemsInThisGroup = multipleItemsInThisGroup, addTopPadding = addTopPadding)
    addBottomPadding(itemLayout = itemLayout, multipleItemsInThisGroup = multipleItemsInThisGroup, showDivider = showDivider)
  }

  private fun renderTimeOfDay(formattedTime: String?) {
    timeTextView.visibility = if (formattedTime != null) View.VISIBLE else View.GONE
    timeTextView.text = formattedTime
  }

  private fun renderDivider(showDivider: Boolean) {
    divider.visibility = GONE
  }

  private fun setIconTint(measurement: BloodPressureMeasurement) {
    val measurementImageTint = when {
      measurement.level.isUrgent() -> R.color.patientsummary_bp_reading_high
      else -> R.color.patientsummary_bp_reading_normal
    }
    heartImageView.imageTintList = ResourcesCompat.getColorStateList(resources, measurementImageTint, null)
  }

  private fun renderBpReading(measurement: BloodPressureMeasurement) {
    val readingsTextAppearanceResId = when {
      measurement.level.isUrgent() -> R.style.Clinic_V2_TextAppearance_PatientSummary_BloodPressure_High
      else -> R.style.Clinic_V2_TextAppearance_PatientSummary_BloodPressure_Normal
    }
    readingsTextView.setTextAppearanceCompat(readingsTextAppearanceResId)
    readingsTextView.text = context.resources.getString(R.string.patientsummary_bp_reading, measurement.systolic, measurement.diastolic)
  }

  private fun renderBpLevel(measurement: BloodPressureMeasurement) {
    levelTextView.text = when (measurement.level.displayTextRes) {
      is Just -> context.getString(measurement.level.displayTextRes.value)
      is None -> ""
    }
  }

  private fun renderRelativeTimestampWithEditButton(
      daysAgo: RelativeTimestamp,
      dateFormatter: DateTimeFormatter,
      isBpEditable: Boolean
  ) {
    val daysAgoText = daysAgo.displayText(context, dateFormatter)

    daysAgoTextView.text = when {
      isBpEditable -> {
        val colorSpanForEditLabel = ForegroundColorSpan(ResourcesCompat.getColor(resources, R.color.blue1, context.theme))
        Truss()
            .pushSpan(colorSpanForEditLabel)
            .append(resources.getString(R.string.patientsummary_edit))
            .popSpan()
            .append(" ${Unicode.bullet} ")
            .append(daysAgoText)
            .build()

      }
      else -> daysAgoText
    }
  }

  private fun addTopPadding(
      itemLayout: ViewGroup,
      multipleItemsInThisGroup: Boolean,
      addTopPadding: Boolean
  ) {
    if (multipleItemsInThisGroup) {
      itemLayout.setPaddingTop(when {
        addTopPadding -> R.dimen.patientsummary_bp_list_item_first_in_group_top_padding
        else -> R.dimen.patientsummary_bp_list_item_multiple_in_group_bp_top_padding
      })
    } else {
      itemLayout.setPaddingTop(R.dimen.patientsummary_bp_list_item_single_group_padding)
    }
  }

  private fun addBottomPadding(
      itemLayout: ViewGroup,
      multipleItemsInThisGroup: Boolean,
      showDivider: Boolean
  ) {
    if (multipleItemsInThisGroup) {
      itemLayout.setPaddingBottom(when {
        showDivider -> R.dimen.patientsummary_bp_list_item_last_in_group_bottom_padding
        else -> R.dimen.patientsummary_bp_list_item_multiple_in_group_bp_bottom_padding
      })
    } else {
      itemLayout.setPaddingBottom(R.dimen.patientsummary_bp_list_item_single_group_padding)
    }
  }
}
