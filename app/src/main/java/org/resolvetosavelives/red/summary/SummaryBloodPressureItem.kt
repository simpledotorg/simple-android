package org.resolvetosavelives.red.summary

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.text.style.StyleSpan
import android.view.View
import android.widget.TextView
import com.xwray.groupie.ViewHolder
import io.reactivex.subjects.Subject
import kotterknife.bindView
import org.resolvetosavelives.red.R
import org.resolvetosavelives.red.bp.BloodPressureMeasurement
import org.resolvetosavelives.red.util.Truss
import org.resolvetosavelives.red.widgets.UiEvent

data class SummaryBloodPressureItem(
    val measurement: BloodPressureMeasurement,
    val timestamp: RelativeTimestamp
) : GroupieItemWithUiEvents<SummaryBloodPressureItem.BpViewHolder>(measurement.uuid.hashCode().toLong()) {

  override lateinit var uiEvents: Subject<UiEvent>

  override fun getLayout() = R.layout.list_patientsummary_bp_measurement

  override fun createViewHolder(itemView: View): BpViewHolder {
    return BpViewHolder(itemView)
  }

  @SuppressLint("SetTextI18n")
  override fun bind(holder: BpViewHolder, position: Int) {
    val context = holder.itemView.context

    val textStyle = when (timestamp) {
      is Today -> StyleSpan(Typeface.BOLD)
      else -> StyleSpan(Typeface.NORMAL)
    }

    holder.readingsTextView.text = "${measurement.systolic}/${measurement.diastolic}"
    holder.categoryTextView.setText(measurement.category().displayTextRes)
    holder.timestampTextView.text = timestamp.displayText(context)

    for (textView in arrayOf(holder.readingsTextView, holder.categoryTextView, holder.timestampTextView)) {
      textView.text = Truss()
          .pushSpan(textStyle)
          .append(textView.text)
          .popSpan()
          .build()
    }
  }

  class BpViewHolder(rootView: View) : ViewHolder(rootView) {
    val readingsTextView by bindView<TextView>(R.id.patientsummary_item_bp_readings)
    val categoryTextView by bindView<TextView>(R.id.patientsummary_item_bp_category)
    val timestampTextView by bindView<TextView>(R.id.patientsummary_item_bp_timestamp)
  }
}
