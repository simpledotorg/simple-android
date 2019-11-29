package org.simple.clinic.summary.prescribeddrugs

import android.view.View
import com.xwray.groupie.ViewHolder
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.list_patientsummary_prescriptions.*
import org.simple.clinic.R
import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.util.RelativeTimestampGenerator
import org.simple.clinic.util.UserClock
import org.simple.clinic.widgets.visibleOrGone
import org.threeten.bp.format.DateTimeFormatter

class DrugsSummaryViewHolder(
    override val containerView: View,
    private val onUpdateClicked: () -> Unit
) : ViewHolder(containerView), LayoutContainer {

  init {
    updateButton.setOnClickListener { onUpdateClicked() }
  }

  fun bind(
      prescriptions: List<PrescribedDrug>,
      dateFormatter: DateTimeFormatter,
      timestampGenerator: RelativeTimestampGenerator,
      userClock: UserClock
  ) {
    summaryViewGroup.visibleOrGone(prescriptions.isNotEmpty())

    setButtonText(prescriptions)

    removeAllDrugViews()

    if (prescriptions.isNotEmpty()) {
      prescriptions
          .map { drug -> DrugView.create(drugsSummaryContainer, drug) }
          .forEach { drugView -> drugsSummaryContainer.addView(drugView, drugsSummaryContainer.childCount - 1) }

      val lastUpdatedPrescription = prescriptions.maxBy { it.updatedAt }!!

      val lastUpdatedTimestamp = timestampGenerator.generate(lastUpdatedPrescription.updatedAt, userClock)

      lastUpdatedTimestampTextView.text = itemView.resources.getString(
          R.string.patientsummary_prescriptions_last_updated,
          lastUpdatedTimestamp.displayText(itemView.context, dateFormatter)
      )
    }
  }

  private fun removeAllDrugViews() {
    drugsSummaryContainer.removeAllViews()
  }

  private fun setButtonText(prescriptions: List<PrescribedDrug>) {
    updateButton.text = if (prescriptions.isEmpty()) {
      itemView.context.getString(R.string.patientsummary_prescriptions_add)
    } else {
      itemView.context.getString(R.string.patientsummary_prescriptions_update)
    }
  }
}
