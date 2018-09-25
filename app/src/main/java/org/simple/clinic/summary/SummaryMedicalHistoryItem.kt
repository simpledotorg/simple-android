package org.simple.clinic.summary

import android.view.View
import com.xwray.groupie.ViewHolder
import io.reactivex.subjects.Subject
import org.simple.clinic.R
import org.simple.clinic.medicalhistory.MedicalHistory
import org.simple.clinic.widgets.UiEvent

data class SummaryMedicalHistoryItem(
    val medicalHistory: MedicalHistory
) : GroupieItemWithUiEvents<SummaryMedicalHistoryItem.HistoryViewHolder>(medicalHistory.uuid.hashCode().toLong()) {

  override lateinit var uiEvents: Subject<UiEvent>

  override fun getLayout() = R.layout.list_patientsummary_medicalhistory

  override fun createViewHolder(itemView: View): HistoryViewHolder {
    return HistoryViewHolder(itemView)
  }

  override fun bind(viewHolder: HistoryViewHolder, position: Int) {

  }

  class HistoryViewHolder(rootView: View) : ViewHolder(rootView) {

  }
}
