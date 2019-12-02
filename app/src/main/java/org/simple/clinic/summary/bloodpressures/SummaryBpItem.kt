package org.simple.clinic.summary.bloodpressures

import android.content.Context
import android.content.res.Resources
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.VisibleForTesting
import androidx.core.content.res.ResourcesCompat
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import io.reactivex.Observable
import io.reactivex.subjects.Subject
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.list_patientsummary_bp_measurement.*
import kotlinx.android.synthetic.main.list_patientsummary_bp_placeholder.*
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.summary.GroupieItemWithUiEvents
import org.simple.clinic.summary.PatientSummaryBpClicked
import org.simple.clinic.summary.SummaryListAdapterIds
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

abstract class SummaryBpViewHolder(rootView: View) : ViewHolder(rootView)

sealed class SummaryBpItem(adapterId: Long) : GroupieItemWithUiEvents<SummaryBpViewHolder>(adapterId) {
  companion object {
    fun from(
        bloodPressureMeasurements: List<BloodPressureMeasurement>,
        utcClock: UtcClock,
        timestampGenerator: RelativeTimestampGenerator,
        dateFormatter: DateTimeFormatter,
        canEditFor: Duration,
        bpTimeFormatter: DateTimeFormatter,
        zoneId: ZoneId,
        userClock: UserClock,
        placeholderLimit: Int
    ): List<SummaryBpItem> {
      val bpListItems = SummaryBloodPressureListItem.from(
          bloodPressures = bloodPressureMeasurements,
          timestampGenerator = timestampGenerator,
          dateFormatter = dateFormatter,
          canEditFor = canEditFor,
          bpTimeFormatter = bpTimeFormatter,
          zoneId = zoneId,
          utcClock = utcClock,
          userClock = userClock
      )
      val placeholderListItems = SummaryBloodPressurePlaceholderListItem.from(
          bloodPressureMeasurements = bloodPressureMeasurements,
          utcClock = utcClock,
          placeholderLimit = placeholderLimit
      )

      return bpListItems + placeholderListItems
    }
  }
}

data class SummaryBloodPressurePlaceholderListItem(
    private val placeholderNumber: Int,
    private val showHint: Boolean = false
) : SummaryBpItem(adapterId = SummaryListAdapterIds.BP_PLACEHOLDER(placeholderNumber)) {

  companion object {
    fun from(
        bloodPressureMeasurements: List<BloodPressureMeasurement>,
        utcClock: UtcClock,
        placeholderLimit: Int
    ): List<SummaryBloodPressurePlaceholderListItem> {
      return Observable.just(bloodPressureMeasurements)
          .map { bpList -> bpList.groupBy { item -> item.recordedAt.atZone(utcClock.zone).toLocalDate() } }
          .map { it.size }
          .map { numberOfBloodPressures ->
            val numberOfPlaceholders = 0.coerceAtLeast(placeholderLimit - numberOfBloodPressures)

            (1..numberOfPlaceholders).map { placeholderNumber ->
              val shouldShowHint = numberOfBloodPressures == 0 && placeholderNumber == 1
              SummaryBloodPressurePlaceholderListItem(placeholderNumber, shouldShowHint)
            }
          }
          .blockingFirst()
    }
  }

  override lateinit var uiEvents: Subject<UiEvent>

  override fun getLayout() = R.layout.list_patientsummary_bp_placeholder

  override fun createViewHolder(itemView: View): SummaryBpViewHolder {
    return BpPlaceholderViewHolder(itemView)
  }

  override fun bind(holder: SummaryBpViewHolder, position: Int) {
    (holder as BpPlaceholderViewHolder).placeHolderMessageTextView.visibility = if (showHint) View.VISIBLE else View.INVISIBLE
  }

  class BpPlaceholderViewHolder(override val containerView: View) : SummaryBpViewHolder(containerView), LayoutContainer
}

data class SummaryBloodPressureListItem(
    val measurement: BloodPressureMeasurement,
    val showDivider: Boolean,
    val formattedTime: String?,
    val addTopPadding: Boolean,
    private val daysAgo: RelativeTimestamp,
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val dateFormatter: DateTimeFormatter,
    val isBpEditable: Boolean
) : SummaryBpItem(measurement.uuid.hashCode().toLong()) {

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

  override fun createViewHolder(itemView: View): SummaryBpViewHolder {
    return BpViewHolder(itemView)
  }

  override fun bind(holder: SummaryBpViewHolder, position: Int) {
    with(holder as BpViewHolder) {
      val context = itemView.context
      val resources = context.resources

      itemView.isClickable = isBpEditable
      itemView.isFocusable = isBpEditable
      if (isBpEditable) itemView.setOnClickListener { uiEvents.onNext(PatientSummaryBpClicked(measurement)) }

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

      daysAgoTextView.text = daysAgoWithEditButton(resources, context, daysAgo)

      val measurementImageTint = when {
        level.isUrgent() -> R.color.patientsummary_bp_reading_high
        else -> R.color.patientsummary_bp_reading_normal
      }
      heartImageView.imageTintList = ResourcesCompat.getColorStateList(resources, measurementImageTint, null)

      divider.visibility = if (showDivider) View.VISIBLE else View.GONE

      timeTextView.visibility = if (formattedTime != null) View.VISIBLE else View.GONE
      timeTextView.text = formattedTime

      val multipleItemsInThisGroup = formattedTime != null
      addTopPadding(itemLayout, multipleItemsInThisGroup)
      addBottomPadding(itemLayout, multipleItemsInThisGroup)
    }
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

  class BpViewHolder(override val containerView: View) : SummaryBpViewHolder(containerView), LayoutContainer
}


