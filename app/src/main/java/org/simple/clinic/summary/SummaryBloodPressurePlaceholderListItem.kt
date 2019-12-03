package org.simple.clinic.summary

import android.view.View
import com.xwray.groupie.ViewHolder
import io.reactivex.Observable
import io.reactivex.subjects.Subject
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.patientsummary_bpplaceholderitem_content.*
import org.simple.clinic.R
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.summary.SummaryListAdapterIds.BP_PLACEHOLDER
import org.simple.clinic.util.UtcClock
import org.simple.clinic.widgets.UiEvent

data class SummaryBloodPressurePlaceholderListItem(
    private val placeholderNumber: Int,
    private val showHint: Boolean = false
) : GroupieItemWithUiEvents<SummaryBloodPressurePlaceholderListItem.BpPlaceholderViewHolder>(adapterId = BP_PLACEHOLDER(placeholderNumber)) {

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

  override fun createViewHolder(itemView: View): BpPlaceholderViewHolder {
    return BpPlaceholderViewHolder(itemView)
  }

  override fun bind(holder: BpPlaceholderViewHolder, position: Int) {
    holder.placeHolderMessageTextView.visibility = if (showHint) View.VISIBLE else View.INVISIBLE
  }

  class BpPlaceholderViewHolder(override val containerView: View) : ViewHolder(containerView), LayoutContainer
}
