package org.simple.clinic.drugs

import android.view.View
import android.widget.RadioButton
import android.widget.TextView
import com.xwray.groupie.ViewHolder
import io.reactivex.subjects.Subject
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.summary.GroupieItemWithUiEvents
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

class ProtocolDrugSelectionItem(
    id: UUID,
    private val name: String,
    private val dosage1: String,
    private val dosage2: String
) : GroupieItemWithUiEvents<ProtocolDrugSelectionItem.DrugViewHolder>(adapterId = id.hashCode().toLong()) {

  override lateinit var uiEvents: Subject<UiEvent>

  override fun getLayout() = R.layout.list_protocol_drug_selection

  override fun createViewHolder(itemView: View): DrugViewHolder {
    val holder = DrugViewHolder(itemView)
    holder.dosage1RadioButton.setOnClickListener {
      ProtocolDrugDosageSelected(name, dosage1)
    }
    holder.dosage2RadioButton.setOnClickListener {
      ProtocolDrugDosageSelected(name, dosage2)
    }
    return holder
  }

  override fun bind(holder: DrugViewHolder, position: Int) {
    holder.nameTextView.text = name
    holder.dosage1RadioButton.text = dosage1
    holder.dosage2RadioButton.text = dosage2
  }

  class DrugViewHolder(rootView: View) : ViewHolder(rootView) {
    val nameTextView by bindView<TextView>(R.id.protocoldrug_name)
    val dosage1RadioButton by bindView<RadioButton>(R.id.protocoldrug_dosage_1)
    val dosage2RadioButton by bindView<RadioButton>(R.id.protocoldrug_dosage_2)
  }
}
