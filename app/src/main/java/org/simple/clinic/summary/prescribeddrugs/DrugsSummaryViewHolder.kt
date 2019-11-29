package org.simple.clinic.summary.prescribeddrugs

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.xwray.groupie.ViewHolder
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.list_patientsummary_prescriptions.*
import org.simple.clinic.R
import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.text.style.TextAppearanceWithLetterSpacingSpan
import org.simple.clinic.util.RelativeTimestampGenerator
import org.simple.clinic.util.Truss
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
      prescriptions.forEach { drug ->
        val drugViewHolder = inflateRowForDrug()
        drugViewHolder.bind(drug)
      }

      val lastUpdatedPrescription = prescriptions.maxBy { it.updatedAt }!!

      val lastUpdatedTimestamp = timestampGenerator.generate(lastUpdatedPrescription.updatedAt, userClock)

      lastUpdatedTimestampTextView.text = itemView.resources.getString(
          R.string.patientsummary_prescriptions_last_updated,
          lastUpdatedTimestamp.displayText(itemView.context, dateFormatter)
      )
    }
  }

  private fun inflateRowForDrug(): DrugViewHolder {
    val drugViewHolder = DrugViewHolder.create(drugsSummaryContainer)
    drugsSummaryContainer.addView(drugViewHolder.itemView, drugsSummaryContainer.childCount - 1)
    return drugViewHolder
  }

  private fun removeAllDrugViews() {
    drugsSummaryContainer.removeAllViews()
  }

  private fun setButtonText(prescriptions: List<PrescribedDrug>) {
    updateButton.text =
        if (prescriptions.isEmpty()) {
          itemView.context.getString(R.string.patientsummary_prescriptions_add)
        } else {
          itemView.context.getString(R.string.patientsummary_prescriptions_update)
        }
  }

  class DrugViewHolder(val itemView: View) {
    private val drugTextView = itemView as TextView

    fun bind(drug: PrescribedDrug) {
      val summaryBuilder = Truss()
      summaryBuilder.append(drug.name)
      if (drug.dosage.isNullOrBlank().not()) {
        val dosageTextAppearance = TextAppearanceWithLetterSpacingSpan(itemView.context, R.style.Clinic_V2_TextAppearance_Body1Left_Grey1)
        summaryBuilder
            .pushSpan(dosageTextAppearance)
            .append("  ${drug.dosage}")
            .popSpan()
      }
      drugTextView.text = summaryBuilder.build()
    }

    companion object {
      fun create(parent: ViewGroup): DrugViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemLayout = inflater.inflate(R.layout.list_patientsummary_prescription_drug, parent, false)
        return DrugViewHolder(itemLayout)
      }
    }
  }
}
