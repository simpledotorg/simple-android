package org.simple.clinic.drugs.selection

import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.TextView
import com.xwray.groupie.GroupieViewHolder
import io.reactivex.subjects.Subject
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.drugs.ProtocolDrugClicked
import org.simple.clinic.summary.GroupieItemWithUiEvents
import org.simple.clinic.widgets.UiEvent

data class ProtocolDrugListItem(
    val id: Int,
    val drugName: String,
    val prescribedDrug: PrescribedDrug?,
    val hideDivider: Boolean
) : GroupieItemWithUiEvents<ProtocolDrugListItem.DrugViewHolder>(adapterId = id.toLong()) {

  override lateinit var uiEvents: Subject<UiEvent>

  override fun getLayout() = R.layout.list_prescribeddrugs_protocol_drug

  override fun createViewHolder(itemView: View): DrugViewHolder {
    return DrugViewHolder(itemView, uiEvents)
  }

  override fun bind(holder: DrugViewHolder, position: Int) {
    holder.nameTextView.text = drugName
    holder.dosageTextView.text = prescribedDrug?.dosage
    holder.itemView.setOnClickListener {
      uiEvents.onNext(ProtocolDrugClicked(drugName, prescribedDrug))
    }
    holder.dividerView.visibility = if (hideDivider) GONE else VISIBLE
  }

  class DrugViewHolder(rootView: View, val uiEvents: Subject<UiEvent>) : GroupieViewHolder(rootView) {
    val nameTextView by bindView<TextView>(R.id.protocoldrug_item_name)
    val dosageTextView by bindView<TextView>(R.id.protocoldrug_item_dosage)
    val dividerView by bindView<View>(R.id.protocoldrug_item_divider)
  }
}
