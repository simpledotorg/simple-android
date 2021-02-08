package org.simple.clinic.drugs.selection.entry

import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.TextView
import com.xwray.groupie.GroupieViewHolder
import io.reactivex.subjects.Subject
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.drugs.CustomPrescriptionClicked
import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.summary.GroupieItemWithUiEvents
import org.simple.clinic.widgets.UiEvent

data class CustomPrescribedDrugListItem(
    val prescription: PrescribedDrug,
    val hideDivider: Boolean
) : GroupieItemWithUiEvents<CustomPrescribedDrugListItem.DrugViewHolder>(prescription.hashCode().toLong()) {

  override lateinit var uiEvents: Subject<UiEvent>

  override fun getLayout() = R.layout.list_prescribeddrugs_custom_drug

  override fun createViewHolder(itemView: View): DrugViewHolder {
    return DrugViewHolder(itemView)
  }

  override fun bind(holder: DrugViewHolder, position: Int) {
    holder.nameTextView.text = prescription.name
    holder.dosageTextView.text = prescription.dosage
    holder.dividerView.visibility = if (hideDivider) GONE else VISIBLE

    holder.itemView.setOnClickListener {
      uiEvents.onNext(CustomPrescriptionClicked(prescription))
    }

  }

  class DrugViewHolder(rootView: View) : GroupieViewHolder(rootView) {
    val nameTextView by bindView<TextView>(R.id.prescribeddrug_item_customdrug_name)
    val dosageTextView by bindView<TextView>(R.id.prescribeddrug_item_customdrug_dosage)
    val dividerView by bindView<View>(R.id.protocoldrug_item_divider)
  }
}
