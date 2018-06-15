package org.simple.clinic.summary

import android.view.View
import android.widget.Button
import com.xwray.groupie.ViewHolder
import io.reactivex.subjects.Subject
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.widgets.UiEvent

class SummaryPrescribedDrugsItem : GroupieItemWithUiEvents<SummaryPrescribedDrugsItem.DrugsViewHolder>(adapterId = 0) {

  override lateinit var uiEvents: Subject<UiEvent>

  override fun getLayout() = R.layout.list_patientsummary_medicines

  override fun createViewHolder(itemView: View): DrugsViewHolder {
    val holder = DrugsViewHolder(itemView)
    holder.updateButton.setOnClickListener {
      uiEvents.onNext(PatientSummaryUpdateDrugsClicked())
    }
    return holder
  }

  override fun bind(viewHolder: DrugsViewHolder, position: Int) {
    // TODO.
  }

  class DrugsViewHolder(rootView: View) : ViewHolder(rootView) {
    val updateButton by bindView<Button>(R.id.patientsummary_item_medicines_update)
  }
}
