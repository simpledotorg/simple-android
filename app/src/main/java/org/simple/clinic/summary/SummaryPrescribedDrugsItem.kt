package org.simple.clinic.summary

import android.view.View
import io.reactivex.subjects.Subject
import org.simple.clinic.R
import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.summary.SummaryListAdapterIds.PRESCRIBED_DRUGS
import org.simple.clinic.summary.prescribeddrugs.DrugsSummaryViewHolder
import org.simple.clinic.util.RelativeTimestampGenerator
import org.simple.clinic.util.UserClock
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.format.DateTimeFormatter

data class SummaryPrescribedDrugsItem(
    val prescriptions: List<PrescribedDrug>,
    val dateFormatter: DateTimeFormatter,
    val userClock: UserClock
) : GroupieItemWithUiEvents<DrugsSummaryViewHolder>(adapterId = PRESCRIBED_DRUGS) {

  private val relativeTimestampGenerator = RelativeTimestampGenerator()

  override lateinit var uiEvents: Subject<UiEvent>

  override fun getLayout() = R.layout.list_patientsummary_prescriptions

  override fun createViewHolder(itemView: View): DrugsSummaryViewHolder {
    return DrugsSummaryViewHolder(itemView) { uiEvents.onNext(PatientSummaryUpdateDrugsClicked()) }
  }

  override fun bind(holder: DrugsSummaryViewHolder, position: Int) {
    holder.bind(prescriptions, dateFormatter, relativeTimestampGenerator, userClock)
  }
}
