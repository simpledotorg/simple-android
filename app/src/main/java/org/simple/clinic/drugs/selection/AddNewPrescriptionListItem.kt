package org.simple.clinic.drugs.selection

import android.view.View
import android.widget.Button
import com.google.android.material.button.MaterialButton
import com.xwray.groupie.ViewHolder
import io.reactivex.subjects.Subject
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.summary.GroupieItemWithUiEvents
import org.simple.clinic.summary.SummaryListAdapterIds.ADD_PRESCRIPTION
import org.simple.clinic.widgets.UiEvent

class AddNewPrescriptionListItem : GroupieItemWithUiEvents<AddNewPrescriptionListItem.AddNewViewHolder>(adapterId = ADD_PRESCRIPTION) {

  override lateinit var uiEvents: Subject<UiEvent>

  override fun getLayout() = R.layout.list_prescribeddrugs_new_custom_drug

  override fun createViewHolder(itemView: View): AddNewViewHolder {
    val holder = AddNewViewHolder(itemView)
    holder.addNewPrescriptionButton.setOnClickListener { uiEvents.onNext(AddNewPrescriptionClicked) }
    return holder
  }

  override fun bind(holder: AddNewViewHolder, position: Int) {
    // Nothing to see here.
  }

  class AddNewViewHolder(rootView: View) : ViewHolder(rootView) {
    val addNewPrescriptionButton by bindView<Button>(R.id.prescribeddrug_item_addnewprescription)
  }
}
