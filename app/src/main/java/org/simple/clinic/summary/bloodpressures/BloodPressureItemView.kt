package org.simple.clinic.summary.bloodpressures

import android.content.Context
import android.content.res.Resources
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.core.content.res.ResourcesCompat
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.patientsummary_bpitem_content.view.*
import org.simple.clinic.R
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.summary.PatientSummaryBpClicked
import org.simple.clinic.util.Just
import org.simple.clinic.util.None
import org.simple.clinic.util.RelativeTimestamp
import org.simple.clinic.util.Truss
import org.simple.clinic.util.Unicode
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.setPaddingBottom
import org.simple.clinic.widgets.setPaddingTop
import org.simple.clinic.widgets.setTextAppearanceCompat
import org.threeten.bp.format.DateTimeFormatter

class BloodPressureItemView(
    context: Context,
    attributeSet: AttributeSet
) : CardView(context, attributeSet) {

  init {
    LayoutInflater.from(context).inflate(R.layout.patientsummary_bpitem_content, this, true)
  }

  fun render(
      isBpEditable: Boolean,
      uiEvents: Subject<UiEvent>,
      measurement: BloodPressureMeasurement,
      daysAgo: RelativeTimestamp,
      showDivider: Boolean,
      formattedTime: String?,
      dateFormatter: DateTimeFormatter,
      addTopPadding: Boolean
  ) {
    isClickable = isBpEditable
    isFocusable = isBpEditable
    if (isBpEditable) setOnClickListener { uiEvents.onNext(PatientSummaryBpClicked(measurement)) }

    val level = measurement.level

    levelTextView.text = when (level.displayTextRes) {
      is Just -> context.getString(level.displayTextRes.value)
      is None -> ""
    }

    val readingsTextAppearanceResId = when {
      level.isUrgent() -> R.style.Clinic_V2_TextAppearance_PatientSummary_BloodPressure_High
      else -> R.style.Clinic_V2_TextAppearance_PatientSummary_BloodPressure_Normal
    }
    readingsTextView.setTextAppearanceCompat(readingsTextAppearanceResId)
    readingsTextView.text = context.resources.getString(R.string.patientsummary_bp_reading, measurement.systolic, measurement.diastolic)

    daysAgoTextView.text = daysAgoWithEditButton(resources, context, daysAgo, dateFormatter, isBpEditable)

    val measurementImageTint = when {
      level.isUrgent() -> R.color.patientsummary_bp_reading_high
      else -> R.color.patientsummary_bp_reading_normal
    }
    heartImageView.imageTintList = ResourcesCompat.getColorStateList(resources, measurementImageTint, null)

    divider.visibility = if (showDivider) View.VISIBLE else View.GONE

    timeTextView.visibility = if (formattedTime != null) View.VISIBLE else View.GONE
    timeTextView.text = formattedTime

    val multipleItemsInThisGroup = formattedTime != null
    addTopPadding(itemLayout = itemLayout, multipleItemsInThisGroup = multipleItemsInThisGroup, addTopPadding = addTopPadding)
    addBottomPadding(itemLayout = itemLayout, multipleItemsInThisGroup = multipleItemsInThisGroup, showDivider = showDivider)
  }

  private fun daysAgoWithEditButton(
      resources: Resources,
      context: Context,
      daysAgo: RelativeTimestamp,
      dateFormatter: DateTimeFormatter,
      isBpEditable: Boolean
  ): CharSequence {
    val daysAgoText = daysAgo.displayText(context, dateFormatter)
    return when {
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
