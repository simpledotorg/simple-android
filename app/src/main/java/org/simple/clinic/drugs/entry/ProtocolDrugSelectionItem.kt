package org.simple.clinic.drugs.entry

import android.view.View
import android.widget.RadioButton
import android.widget.TextView
import com.xwray.groupie.ViewHolder
import io.reactivex.subjects.Subject
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.summary.GroupieItemWithUiEvents
import org.simple.clinic.widgets.UiEvent
import timber.log.Timber

data class ProtocolDrugSelectionItem(
    val id: Int,
    val name: String,
    val option1: DosageOption,
    val option2: DosageOption
) : GroupieItemWithUiEvents<ProtocolDrugSelectionItem.DrugViewHolder>(adapterId = id.toLong()) {

  init {
    if (option1.isSelected && option2.isSelected) {
      Timber.e(AssertionError("Both dosage options are selected for $name"))
    }
  }

  override lateinit var uiEvents: Subject<UiEvent>

  override fun getLayout() = R.layout.list_prescribeddrugs_protocol_drug

  override fun createViewHolder(itemView: View): DrugViewHolder {
    val holder = DrugViewHolder(itemView)
    holder.dosage1RadioButton.setOnClickListener {
      ProtocolDrugDosageSelected(name, option1.dosage)
    }
    holder.dosage2RadioButton.setOnClickListener {
      ProtocolDrugDosageSelected(name, option2.dosage)
    }
    return holder
  }

  override fun bind(holder: DrugViewHolder, position: Int) {
    holder.nameTextView.text = name
    holder.dosage1RadioButton.text = option1.dosage
    holder.dosage2RadioButton.text = option2.dosage
    holder.dosage1RadioButton.isChecked = option1.isSelected
    holder.dosage2RadioButton.isChecked = option2.isSelected
  }

  data class DosageOption(val dosage: String, val isSelected: Boolean)

  class DrugViewHolder(rootView: View) : ViewHolder(rootView) {
    val nameTextView by bindView<TextView>(R.id.prescribeddrug_item_protocoldrug_name)
    val dosage1RadioButton by bindView<RadioButton>(R.id.prescribeddrug_item_protocoldrug_dosage_1)
    val dosage2RadioButton by bindView<RadioButton>(R.id.prescribeddrug_item_protocoldrug_dosage_2)
  }
}
