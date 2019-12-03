package org.simple.clinic.summary

import android.content.Context
import android.content.res.Resources
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.VisibleForTesting
import androidx.core.content.res.ResourcesCompat
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import io.reactivex.subjects.Subject
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.list_patientsummary_bp_measurement.*
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.util.Just
import org.simple.clinic.util.None
import org.simple.clinic.util.RelativeTimestamp
import org.simple.clinic.util.RelativeTimestampGenerator
import org.simple.clinic.util.Truss
import org.simple.clinic.util.Unicode
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.UtcClock
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.setPaddingBottom
import org.simple.clinic.widgets.setPaddingTop
import org.simple.clinic.widgets.setTextAppearanceCompat
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter

data class SummaryBloodPressureListItem(
    val measurement: BloodPressureMeasurement,
    val showDivider: Boolean,
    val formattedTime: String?,
    val addTopPadding: Boolean,
    private val daysAgo: RelativeTimestamp,
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val dateFormatter: DateTimeFormatter,
    val isBpEditable: Boolean
) : GroupieItemWithUiEvents<SummaryBloodPressureListItem.BpViewHolder>(measurement.uuid.hashCode().toLong()) {

  companion object {
    fun from(
        bloodPressures: List<BloodPressureMeasurement>,
        timestampGenerator: RelativeTimestampGenerator,
        dateFormatter: DateTimeFormatter,
        canEditFor: Duration,
        bpTimeFormatter: DateTimeFormatter,
        zoneId: ZoneId,
        utcClock: UtcClock,
        userClock: UserClock
    ): List<SummaryBloodPressureListItem> {
      val measurementsByDate = bloodPressures.groupBy { item -> item.recordedAt.atZone(utcClock.zone).toLocalDate() }

      return measurementsByDate.mapValues { (_, measurementList) ->
        measurementList.map { measurement ->
          val timestamp = timestampGenerator.generate(measurement.recordedAt, userClock)
          SummaryBloodPressureListItem(
              measurement = measurement,
              showDivider = measurement == measurementList.last(),
              formattedTime = if (measurementList.size > 1) displayTime(measurement.recordedAt, zoneId, bpTimeFormatter) else null,
              addTopPadding = measurement == measurementList.first(),
              daysAgo = timestamp,
              dateFormatter = dateFormatter,
              isBpEditable = isBpEditable(measurement, canEditFor, utcClock)
          )
        }
      }.values.flatten()
    }

    private fun displayTime(
        instant: Instant,
        zoneId: ZoneId,
        formatter: DateTimeFormatter
    ): String = instant.atZone(zoneId).format(formatter)

    private fun isBpEditable(
        bloodPressureMeasurement: BloodPressureMeasurement,
        bpEditableFor: Duration,
        utcClock: UtcClock
    ): Boolean {
      val now = Instant.now(utcClock)
      val createdAt = bloodPressureMeasurement.createdAt

      val durationSinceBpCreated = Duration.between(createdAt, now)

      return durationSinceBpCreated <= bpEditableFor
    }
  }

  override lateinit var uiEvents: Subject<UiEvent>

  override fun getLayout() = R.layout.list_patientsummary_bp_measurement

  override fun createViewHolder(itemView: View): BpViewHolder {
    return BpViewHolder(itemView)
  }

  override fun bind(holder: BpViewHolder, position: Int) {
    val context = holder.itemView.context
    val resources = context.resources

    holder.itemView.isClickable = isBpEditable
    holder.itemView.isFocusable = isBpEditable
    if (isBpEditable) holder.itemView.setOnClickListener { uiEvents.onNext(PatientSummaryBpClicked(measurement)) }

    val level = measurement.level

    holder.levelTextView.text = when (level.displayTextRes) {
      is Just -> context.getString(level.displayTextRes.value)
      is None -> ""
    }

    val readingsTextAppearanceResId = when {
      level.isUrgent() -> R.style.Clinic_V2_TextAppearance_PatientSummary_BloodPressure_High
      else -> R.style.Clinic_V2_TextAppearance_PatientSummary_BloodPressure_Normal
    }
    holder.readingsTextView.setTextAppearanceCompat(readingsTextAppearanceResId)
    holder.readingsTextView.text = context.resources.getString(R.string.patientsummary_bp_reading, measurement.systolic, measurement.diastolic)

    holder.daysAgoTextView.text = daysAgoWithEditButton(resources, context, daysAgo)

    val measurementImageTint = when {
      level.isUrgent() -> R.color.patientsummary_bp_reading_high
      else -> R.color.patientsummary_bp_reading_normal
    }
    holder.heartImageView.imageTintList = ResourcesCompat.getColorStateList(resources, measurementImageTint, null)

    holder.divider.visibility = if (showDivider) VISIBLE else GONE

    holder.timeTextView.visibility = if (formattedTime != null) VISIBLE else GONE
    holder.timeTextView.text = formattedTime

    val multipleItemsInThisGroup = formattedTime != null
    addTopPadding(holder.itemLayout, multipleItemsInThisGroup)
    addBottomPadding(holder.itemLayout, multipleItemsInThisGroup)
  }

  private fun daysAgoWithEditButton(
      resources: Resources,
      context: Context,
      daysAgo: RelativeTimestamp
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

  class BpViewHolder(override val containerView: View) : ViewHolder(containerView), LayoutContainer
}
