package org.resolvetosavelives.red.summary

import android.view.View
import android.widget.Button
import io.reactivex.subjects.Subject
import kotterknife.bindView
import org.resolvetosavelives.red.R
import org.resolvetosavelives.red.widgets.UiEvent

class SummaryAddNewBpItem : GroupieItemWithUiEvents<SummaryAddNewBpItem.NewBpViewHolder>(adapterId = 1) {

  override lateinit var uiEvents: Subject<UiEvent>

  override fun getLayout() = R.layout.list_patientsummary_add_new_bp_measurement

  override fun createViewHolder(itemView: View): NewBpViewHolder {
    val holder = NewBpViewHolder(itemView)
    holder.newBpButton.setOnClickListener {
      uiEvents.onNext(PatientSummaryNewBpClicked())
    }
    return holder
  }

  override fun bind(holder: NewBpViewHolder, position: Int) {}

  class NewBpViewHolder(rootView: View) : com.xwray.groupie.ViewHolder(rootView) {
    val newBpButton by bindView<Button>(R.id.patientsummary_item_newbp)
  }
}
