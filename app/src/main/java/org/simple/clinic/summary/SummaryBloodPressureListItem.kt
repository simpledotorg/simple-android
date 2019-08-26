package org.simple.clinic.summary

import android.annotation.SuppressLint
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import io.reactivex.subjects.Subject
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.util.Just
import org.simple.clinic.util.None
import org.simple.clinic.util.RelativeTimestamp
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.setPaddingBottom
import org.simple.clinic.widgets.setPaddingTop
import org.simple.clinic.widgets.setTextAppearanceCompat

data class SummaryBloodPressureListItem(
    val measurement: BloodPressureMeasurement,
    private val daysAgo: RelativeTimestamp,
    val showDivider: Boolean,
    val formattedTime: String?,
    val addTopPadding: Boolean
) : GroupieItemWithUiEvents<SummaryBloodPressureListItem.BpViewHolder>(measurement.uuid.hashCode().toLong()) {

  override lateinit var uiEvents: Subject<UiEvent>

  override fun getLayout() = R.layout.list_patientsummary_bp_measurement

  override fun createViewHolder(itemView: View): BpViewHolder {
    return BpViewHolder(itemView)
  }

  @SuppressLint("SetTextI18n")
  override fun bind(holder: BpViewHolder, position: Int) {
    val context = holder.itemView.context
    val resources = context.resources

    val level = measurement.level

    holder.levelTextView.text = when (level.displayTextRes) {
      is Just -> context.getString(level.displayTextRes.value)
      is None -> ""
    }

    val bpReading = context.resources.getString(R.string.patientsummary_bp_reading, measurement.systolic, measurement.diastolic)
    holder.readingsTextView.text = bpReading

    val readingsTextAppearanceResId = when {
      level.isUrgent() -> R.style.Clinic_V2_TextAppearance_PatientSummary_BloodPressure_High
      else -> R.style.Clinic_V2_TextAppearance_PatientSummary_BloodPressure_Normal
    }
    holder.readingsTextView.setTextAppearanceCompat(readingsTextAppearanceResId)

    holder.daysAgoTextView.text = daysAgo.displayText(context)

    val measurementImageTint = when {
      level.isUrgent() -> R.color.patientsummary_bp_reading_high
      else -> R.color.patientsummary_bp_reading_normal
    }
    holder.heartImageView.imageTintList = ResourcesCompat.getColorStateList(resources, measurementImageTint, null)

    holder.itemView.setOnClickListener { uiEvents.onNext(PatientSummaryBpClicked(measurement)) }

    holder.divider.visibility = if (showDivider) VISIBLE else GONE

    holder.timeTextView.visibility = when {
      formattedTime != null -> VISIBLE
      else -> GONE
    }

    holder.timeTextView.text = when {
      formattedTime != null -> formattedTime
      else -> null
    }

    val multipleItemsInThisGroup = formattedTime != null
    addTopPadding(holder.itemLayout, multipleItemsInThisGroup)
    addBottomPadding(holder.itemLayout, multipleItemsInThisGroup)
  }

  private fun addTopPadding(itemLayout: ViewGroup, multipleItemsInThisGroup: Boolean) {
    if (multipleItemsInThisGroup) {
      itemLayout.setPaddingTop(when {
        addTopPadding -> R.dimen.patientsummary_bp_list_item_first_in_group_top_padding
        else -> R.dimen.patientsummary_bp_list_item_multiple_in_group_bp_top_padding
      })
    } else {
      itemLayout.setPaddingTop(R.dimen.patientsummary_bp_list_item_single_group_padding)
    }
  }

  private fun addBottomPadding(itemLayout: ViewGroup, multipleItemsInThisGroup: Boolean) {
    if (multipleItemsInThisGroup) {
      itemLayout.setPaddingBottom(when {
        showDivider -> R.dimen.patientsummary_bp_list_item_last_in_group_bottom_padding
        else -> R.dimen.patientsummary_bp_list_item_multiple_in_group_bp_bottom_padding
      })
    } else {
      itemLayout.setPaddingBottom(R.dimen.patientsummary_bp_list_item_single_group_padding)
    }
  }

  override fun isSameAs(other: Item<*>?): Boolean {
    return this == other
  }

  class BpViewHolder(rootView: View) : ViewHolder(rootView) {
    val readingsTextView by bindView<TextView>(R.id.patientsummary_item_bp_readings)
    val heartImageView by bindView<ImageView>(R.id.patientsummary_bp_reading_heart)
    val levelTextView by bindView<TextView>(R.id.patientsummary_item_bp_level)
    val daysAgoTextView by bindView<TextView>(R.id.patientsummary_item_bp_days_ago)
    val divider by bindView<View>(R.id.patientsummary_item_bp_divider)
    val timeTextView by bindView<TextView>(R.id.patientsummary_item_bp_time)
    val itemLayout by bindView<LinearLayout>(R.id.patientsummary_item_layout)
  }
}
