package org.simple.clinic.drugs.selection

import android.view.View
import android.widget.TextView
import com.xwray.groupie.ViewHolder
import io.reactivex.subjects.Subject
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.summary.GroupieItemWithUiEvents
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

data class CustomPrescribedDrugItem(
    val adapterId: UUID,
    val name: String,
    val dosage: String?
) : GroupieItemWithUiEvents<CustomPrescribedDrugItem.DrugViewHolder>(adapterId.hashCode().toLong()) {

  override lateinit var uiEvents: Subject<UiEvent>

  override fun getLayout() = R.layout.list_prescribeddrugs_custom_drug

  override fun createViewHolder(itemView: View): DrugViewHolder {
    return DrugViewHolder(itemView)
  }

  override fun bind(holder: DrugViewHolder, position: Int) {
    holder.nameTextView.text = name
    holder.dosageTextView.text = dosage
  }

  class DrugViewHolder(rootView: View) : ViewHolder(rootView) {
    val nameTextView by bindView<TextView>(R.id.prescribeddrug_item_customdrug_name)
    val dosageTextView by bindView<TextView>(R.id.prescribeddrug_item_customdrug_dosage)
  }
}
