package org.simple.clinic.summary

import android.view.View
import android.widget.Button
import io.reactivex.subjects.Subject
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.widgets.UiEvent

class SummaryMedicineItem : GroupieItemWithUiEvents<SummaryMedicineItem.MedicinesViewHolder>(adapterId = 0) {

  override lateinit var uiEvents: Subject<UiEvent>

  override fun getLayout() = R.layout.list_patientsummary_medicines

  override fun createViewHolder(itemView: View): MedicinesViewHolder {
    val holder = MedicinesViewHolder(itemView)
    holder.updateButton.setOnClickListener {
      uiEvents.onNext(PatientSummaryUpdateMedicinesClicked())
    }
    return holder
  }

  override fun bind(viewHolder: MedicinesViewHolder, position: Int) {
    // TODO.
  }

  class MedicinesViewHolder(rootView: View) : com.xwray.groupie.ViewHolder(rootView) {
    val updateButton by bindView<Button>(R.id.patientsummary_item_medicines_update)
  }
}
