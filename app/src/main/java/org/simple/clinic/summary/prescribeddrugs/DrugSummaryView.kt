package org.simple.clinic.summary.prescribeddrugs

import android.content.Context
import android.util.AttributeSet
import androidx.cardview.widget.CardView
import kotlinx.android.synthetic.main.list_patientsummary_prescriptions.view.*
import org.simple.clinic.R
import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.util.RelativeTimestampGenerator
import org.simple.clinic.util.UserClock
import org.simple.clinic.widgets.visibleOrGone
import org.threeten.bp.format.DateTimeFormatter

class DrugSummaryView(context: Context, attributeSet: AttributeSet) : CardView(context, attributeSet) {

  private val timestampGenerator = RelativeTimestampGenerator()

  fun bind(
      prescriptions: List<PrescribedDrug>,
      dateFormatter: DateTimeFormatter,
      userClock: UserClock,
      updateClicked: () -> Unit
  ) {
    updateButton.setOnClickListener { updateClicked() }

    summaryViewGroup.visibleOrGone(prescriptions.isNotEmpty())

    setButtonText(prescriptions)

    removeAllDrugViews()

    if (prescriptions.isNotEmpty()) {
      prescriptions
          .map { drug -> DrugView.create(drugsSummaryContainer, drug) }
          .forEach { drugView -> drugsSummaryContainer.addView(drugView, drugsSummaryContainer.childCount - 1) }

      val lastUpdatedPrescription = prescriptions.maxBy { it.updatedAt }!!

      val lastUpdatedTimestamp = timestampGenerator.generate(lastUpdatedPrescription.updatedAt, userClock)

      lastUpdatedTimestampTextView.text = resources.getString(
          R.string.patientsummary_prescriptions_last_updated,
          lastUpdatedTimestamp.displayText(context, dateFormatter)
      )
    }
  }

  private fun removeAllDrugViews() {
    drugsSummaryContainer.removeAllViews()
  }

  private fun setButtonText(prescriptions: List<PrescribedDrug>) {
    updateButton.text = if (prescriptions.isEmpty()) {
      context.getString(R.string.patientsummary_prescriptions_add)
    } else {
      context.getString(R.string.patientsummary_prescriptions_update)
    }
  }
}
