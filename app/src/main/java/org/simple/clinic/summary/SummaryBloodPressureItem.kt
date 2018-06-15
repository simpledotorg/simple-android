package org.simple.clinic.summary

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.support.v4.content.ContextCompat
import android.text.style.StyleSpan
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.xwray.groupie.ViewHolder
import io.reactivex.subjects.Subject
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.util.Just
import org.simple.clinic.util.None
import org.simple.clinic.util.Truss
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.setTopPadding

data class SummaryBloodPressureItem(
    val measurement: BloodPressureMeasurement,
    val timestamp: RelativeTimestamp,
    val firstItem: Boolean
) : GroupieItemWithUiEvents<SummaryBloodPressureItem.BpViewHolder>(measurement.uuid.hashCode().toLong()) {

  override lateinit var uiEvents: Subject<UiEvent>

  override fun getLayout() = R.layout.list_patientsummary_bp_measurement

  override fun createViewHolder(itemView: View): BpViewHolder {
    return BpViewHolder(itemView)
  }

  @SuppressLint("SetTextI18n")
  override fun bind(holder: BpViewHolder, position: Int) {
    val context = holder.itemView.context

    val textStyleSpan = when (timestamp) {
      is Today -> StyleSpan(Typeface.BOLD)
      else -> StyleSpan(Typeface.NORMAL)
    }

    val riskLevel = measurement.riskLevel

    val riskLevelTextColor = when {
      riskLevel.isUrgent() -> ContextCompat.getColor(context, R.color.patientsummary_high_or_worse_blood_pressure)
      else -> holder.originalTextColor
    }

    holder.riskLevelTextView.text = when (riskLevel.displayTextRes) {
      is Just -> context.getString(riskLevel.displayTextRes.value)
      is None -> ""
    }
    holder.readingsTextView.text = "${measurement.systolic}/${measurement.diastolic}"
    holder.timestampTextView.text = timestamp.displayText(context)

    for (textView in arrayOf(holder.readingsTextView, holder.riskLevelTextView, holder.timestampTextView)) {
      textView.text = Truss()
          .pushSpan(textStyleSpan)
          .append(textView.text)
          .popSpan()
          .build()
    }

    for (textView in arrayOf(holder.readingsTextView, holder.riskLevelTextView)) {
      textView.setTextColor(riskLevelTextColor)
    }

    holder.containerViewGroup.setTopPadding(when {
      firstItem -> 0
      else -> holder.originalContentTopPadding
    })
  }

  class BpViewHolder(rootView: View) : ViewHolder(rootView) {
    val readingsTextView by bindView<TextView>(R.id.patientsummary_item_bp_readings)
    val riskLevelTextView by bindView<TextView>(R.id.patientsummary_item_bp_risk_level)
    val timestampTextView by bindView<TextView>(R.id.patientsummary_item_bp_timestamp)
    val containerViewGroup by bindView<ViewGroup>(R.id.patientsummary_item_bp_content_container)

    val originalTextColor by lazy { readingsTextView.currentTextColor }
    val originalContentTopPadding by lazy { containerViewGroup.paddingTop }
  }
}
