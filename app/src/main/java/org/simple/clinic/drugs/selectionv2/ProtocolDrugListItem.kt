package org.simple.clinic.drugs.selectionv2

import android.view.View
import android.widget.TextView
import com.xwray.groupie.ViewHolder
import io.reactivex.subjects.Subject
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.drugs.selection.ProtocolDrugDosageSelected
import org.simple.clinic.summary.GroupieItemWithUiEvents
import org.simple.clinic.widgets.UiEvent

data class ProtocolDrugListItem(
    val id: Int,
    val drugName: String,
    val dosage: String?
) : GroupieItemWithUiEvents<ProtocolDrugListItem.DrugViewHolder>(adapterId = id.toLong()) {

  override lateinit var uiEvents: Subject<UiEvent>

  override fun getLayout() = R.layout.list_prescribeddrugs_protocol_drug_v2

  override fun createViewHolder(itemView: View): DrugViewHolder {
    return DrugViewHolder(itemView, uiEvents)
  }

  override fun bind(holder: DrugViewHolder, position: Int) {
    holder.nameTextView.text = drugName
    holder.dosageTextView.text = dosage ?: ""
    holder.rootView.setOnClickListener {
      uiEvents.onNext(ProtocolDrugSelected(drugName))
    }
  }

  class DrugViewHolder(val rootView: View, val uiEvents: Subject<UiEvent>) : ViewHolder(rootView) {
    val nameTextView by bindView<TextView>(R.id.protocoldrug_item_name)
    val dosageTextView by bindView<TextView>(R.id.protocoldrug_item_dosage)
  }
}
