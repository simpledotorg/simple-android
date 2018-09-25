package org.simple.clinic.summary

import android.view.View
import android.widget.TextView
import com.xwray.groupie.ViewHolder
import io.reactivex.subjects.Subject
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.medicalhistory.MedicalHistory
import org.simple.clinic.widgets.UiEvent
import java.util.Locale.ENGLISH

data class SummaryMedicalHistoryItem(
    val medicalHistory: MedicalHistory,
    val lastUpdatedAt: RelativeTimestamp
) : GroupieItemWithUiEvents<SummaryMedicalHistoryItem.HistoryViewHolder>(medicalHistory.uuid.hashCode().toLong()) {

  override lateinit var uiEvents: Subject<UiEvent>

  override fun getLayout() = R.layout.list_patientsummary_medicalhistory

  override fun createViewHolder(itemView: View): HistoryViewHolder {
    return HistoryViewHolder(itemView)
  }

  override fun bind(holder: HistoryViewHolder, position: Int) {
    val context = holder.itemView.context
    holder.lastUpdatedAtTextView.text = context.getString(
        R.string.patientsummary_medicalhistory_last_updated,
        lastUpdatedAt.displayText(context).toLowerCase(ENGLISH))
  }

  class HistoryViewHolder(rootView: View) : ViewHolder(rootView) {
    val lastUpdatedAtTextView by bindView<TextView>(R.id.patientsummary_medicalhistory_last_update_timestamp)
  }
}
