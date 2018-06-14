package org.resolvetosavelives.red.summary

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.widget.TextView
import com.xwray.groupie.ViewHolder
import io.reactivex.subjects.Subject
import kotterknife.bindView
import org.resolvetosavelives.red.R
import org.resolvetosavelives.red.bp.BloodPressureMeasurement
import org.resolvetosavelives.red.widgets.UiEvent
import org.threeten.bp.Instant
import org.threeten.bp.Period
import org.threeten.bp.ZoneOffset.UTC
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.temporal.ChronoUnit.DAYS
import org.threeten.bp.temporal.ChronoUnit.MONTHS
import java.util.Locale

data class SummaryBloodPressureItem(
    val measurement: BloodPressureMeasurement
) : GroupieItemWithUiEvents<SummaryBloodPressureItem.BpViewHolder>(measurement.uuid.hashCode().toLong()) {

  override lateinit var uiEvents: Subject<UiEvent>

  override fun getLayout() = R.layout.list_patientsummary_bp_measurement

  override fun createViewHolder(itemView: View): BpViewHolder {
    return BpViewHolder(itemView)
  }

  @SuppressLint("SetTextI18n")
  override fun bind(holder: BpViewHolder, position: Int) {
    val context = holder.itemView.context
    holder.readingsTextView.text = "${measurement.systolic}/${measurement.diastolic}"
    holder.categoryTextView.setText(measurement.category().displayTextRes)
    holder.timestampTextView.text = generateTimestamp(context, measurement.updatedAt)
  }

  private fun generateTimestamp(context: Context, time: Instant): String {
    val now = Instant.now()
    return when {
      time > now.minus(1, DAYS) -> context.getString(R.string.timestamp_today)
      time > now.minus(2, DAYS) -> context.getString(R.string.timestamp_yesterday)
      time > now.minus(6, MONTHS) -> {
        val updatedAtDate = time.atZone(UTC).toLocalDate()
        val nowDate = now.atZone(UTC).toLocalDate()
        val period = Period.between(nowDate, updatedAtDate)
        context.getString(R.string.timestamp_days, period.days)
      }
      else -> {
        timestampFormatter.format(time)
      }
    }
  }

  companion object {
    val timestampFormatter = DateTimeFormatter.ofPattern("d MMM, yyyy", Locale.ENGLISH)!!
  }

  class BpViewHolder(rootView: View) : ViewHolder(rootView) {
    val readingsTextView by bindView<TextView>(R.id.patientsummary_item_bp_readings)
    val categoryTextView by bindView<TextView>(R.id.patientsummary_item_bp_category)
    val timestampTextView by bindView<TextView>(R.id.patientsummary_item_bp_timestamp)
  }
}
