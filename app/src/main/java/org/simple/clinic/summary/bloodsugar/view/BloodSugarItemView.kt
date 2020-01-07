package org.simple.clinic.summary.bloodsugar.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import kotlinx.android.synthetic.main.patientsummary_bloodsugar_item_content.view.*
import org.simple.clinic.R
import org.simple.clinic.bloodsugar.BloodSugarMeasurement
import org.simple.clinic.bloodsugar.BloodSugarReading
import org.simple.clinic.util.RelativeTimestamp
import org.simple.clinic.widgets.setPaddingBottom
import org.simple.clinic.widgets.setPaddingTop
import org.threeten.bp.format.DateTimeFormatter

class BloodSugarItemView(
    context: Context,
    attributeSet: AttributeSet
) : FrameLayout(context, attributeSet) {

  init {
    LayoutInflater.from(context).inflate(R.layout.patientsummary_bloodsugar_item_content, this, true)
  }

  fun render(
      measurement: BloodSugarMeasurement,
      daysAgo: RelativeTimestamp,
      formattedTime: String?,
      dateFormatter: DateTimeFormatter,
      addTopPadding: Boolean
  ) {
    renderBloodSugarReading(measurement.reading)
    renderRelativeTimestampWithEditButton(daysAgo, dateFormatter)
    renderTimeOfDay(formattedTime)
    addPadding(formattedTime, addTopPadding)
  }

  private fun addPadding(formattedTime: String?, addTopPadding: Boolean) {
    val multipleItemsInThisGroup = formattedTime != null
    addTopPadding(itemLayout = itemLayout, multipleItemsInThisGroup = multipleItemsInThisGroup, addTopPadding = addTopPadding)
    addBottomPadding(itemLayout = itemLayout, multipleItemsInThisGroup = multipleItemsInThisGroup)
  }

  private fun renderTimeOfDay(formattedTime: String?) {
    timeTextView.visibility = if (formattedTime != null) View.VISIBLE else View.GONE
    timeTextView.text = formattedTime
  }

  private fun renderBloodSugarReading(reading: BloodSugarReading) {
    readingTextView.text = context.getString(R.string.bloodsugarsummaryview_reading_unit_type, reading.value, reading.type)
  }

  private fun renderRelativeTimestampWithEditButton(
      daysAgo: RelativeTimestamp,
      dateFormatter: DateTimeFormatter
  ) {
    daysAgoTextView.text = daysAgo.displayText(context, dateFormatter)
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
      multipleItemsInThisGroup: Boolean
  ) {
    val paddingResourceId = if (multipleItemsInThisGroup) {
      R.dimen.patientsummary_bp_list_item_multiple_in_group_bp_bottom_padding
    } else {
      R.dimen.patientsummary_bp_list_item_single_group_padding
    }

    itemLayout.setPaddingBottom(paddingResourceId)
  }
}
