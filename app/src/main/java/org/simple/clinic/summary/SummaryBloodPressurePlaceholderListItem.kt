package org.simple.clinic.summary

import android.view.View
import com.xwray.groupie.ViewHolder
import io.reactivex.Observable
import io.reactivex.subjects.Subject
import org.simple.clinic.R
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.summary.SummaryListAdapterIds.BP_PLACEHOLDER
import org.simple.clinic.summary.bloodpressures.BloodPressurePlaceholderItemView
import org.simple.clinic.util.UtcClock
import org.simple.clinic.widgets.UiEvent

data class SummaryBloodPressurePlaceholderListItem(
    private val placeholderNumber: Int,
    private val showHint: Boolean,
    private val showDivider: Boolean
) : GroupieItemWithUiEvents<ViewHolder>(adapterId = BP_PLACEHOLDER(placeholderNumber)) {

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
              val shouldShowDivider = placeholderNumber != numberOfPlaceholders

              SummaryBloodPressurePlaceholderListItem(placeholderNumber, shouldShowHint, shouldShowDivider)
            }
          }
          .blockingFirst()
    }
  }

  override lateinit var uiEvents: Subject<UiEvent>

  override fun getLayout() = R.layout.list_patientsummary_bp_placeholder

  override fun createViewHolder(itemView: View): ViewHolder {
    return ViewHolder(itemView)
  }

  override fun bind(holder: ViewHolder, position: Int) {
    (holder.itemView as BloodPressurePlaceholderItemView).render(showHint = showHint, showDivider = showDivider)
  }
}
