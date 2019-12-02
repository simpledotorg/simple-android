package org.simple.clinic.summary

import android.view.View
import com.xwray.groupie.ViewHolder
import io.reactivex.subjects.Subject
import org.simple.clinic.R
import org.simple.clinic.medicalhistory.MedicalHistory
import org.simple.clinic.summary.medicalhistory.MedicalHistorySummaryView
import org.simple.clinic.util.RelativeTimestamp
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.format.DateTimeFormatter

data class SummaryMedicalHistoryItem(
    val medicalHistory: MedicalHistory,
    val lastUpdatedAt: RelativeTimestamp,
    val dateFormatter: DateTimeFormatter
) : GroupieItemWithUiEvents<ViewHolder>(medicalHistory.uuid.hashCode().toLong()) {

  override lateinit var uiEvents: Subject<UiEvent>

  override fun getLayout() = R.layout.list_patientsummary_medicalhistory

  override fun createViewHolder(itemView: View): ViewHolder {
    return ViewHolder(itemView)
  }

  override fun bind(holder: ViewHolder, position: Int) {
    (holder.itemView as MedicalHistorySummaryView).bind(medicalHistory, lastUpdatedAt, dateFormatter) { question, newAnswer ->
      uiEvents.onNext(SummaryMedicalHistoryAnswerToggled(question, newAnswer))
    }
  }
}
