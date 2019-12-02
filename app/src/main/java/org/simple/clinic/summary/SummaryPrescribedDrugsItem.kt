package org.simple.clinic.summary

import android.view.View
import com.xwray.groupie.ViewHolder
import io.reactivex.subjects.Subject
import org.simple.clinic.R
import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.summary.SummaryListAdapterIds.PRESCRIBED_DRUGS
import org.simple.clinic.summary.prescribeddrugs.DrugSummaryView
import org.simple.clinic.util.UserClock
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.format.DateTimeFormatter

data class SummaryPrescribedDrugsItem(
    val prescriptions: List<PrescribedDrug>,
    val dateFormatter: DateTimeFormatter,
    val userClock: UserClock
) : GroupieItemWithUiEvents<ViewHolder>(adapterId = PRESCRIBED_DRUGS) {

  override lateinit var uiEvents: Subject<UiEvent>

  override fun getLayout() = R.layout.list_patientsummary_prescriptions

  override fun createViewHolder(itemView: View): ViewHolder {
    return ViewHolder(itemView)
  }

  override fun bind(holder: ViewHolder, position: Int) {
    val drugSummaryView = holder.itemView as DrugSummaryView
    drugSummaryView.bind(prescriptions, dateFormatter, userClock) { uiEvents.onNext(PatientSummaryUpdateDrugsClicked()) }
  }
}
