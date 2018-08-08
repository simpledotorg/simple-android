package org.simple.clinic.drugs.selection

import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import com.xwray.groupie.ViewHolder
import io.reactivex.subjects.Subject
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.summary.GroupieItemWithUiEvents
import org.simple.clinic.widgets.UiEvent

data class CustomPrescribedDrugListItem(
    val prescription: PrescribedDrug
) : GroupieItemWithUiEvents<CustomPrescribedDrugListItem.DrugViewHolder>(prescription.hashCode().toLong()) {

  override lateinit var uiEvents: Subject<UiEvent>

  override fun getLayout() = R.layout.list_prescribeddrugs_custom_drug

  override fun createViewHolder(itemView: View): DrugViewHolder {
    val holder = DrugViewHolder(itemView)
    holder.deleteButton.setOnClickListener {
      uiEvents.onNext(DeleteCustomPrescriptionClicked(prescription))
    }
    return holder
  }

  override fun bind(holder: DrugViewHolder, position: Int) {
    holder.nameTextView.text = prescription.name
    holder.dosageTextView.text = prescription.dosage
  }

  class DrugViewHolder(rootView: View) : ViewHolder(rootView) {
    val nameTextView by bindView<TextView>(R.id.prescribeddrug_item_customdrug_name)
    val dosageTextView by bindView<TextView>(R.id.prescribeddrug_item_customdrug_dosage)
    val deleteButton by bindView<ImageButton>(R.id.prescribeddrug_item_customdrug_delete)
  }
}
