package org.simple.clinic.drugs.entry

import android.view.View
import android.widget.RadioButton
import android.widget.TextView
import com.xwray.groupie.ViewHolder
import io.reactivex.subjects.Subject
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.protocol.ProtocolDrug
import org.simple.clinic.summary.GroupieItemWithUiEvents
import org.simple.clinic.widgets.UiEvent
import timber.log.Timber

data class ProtocolDrugSelectionItem constructor(
    val id: Int,
    val drug: ProtocolDrug,
    val option1: DosageOption,
    val option2: DosageOption
) : GroupieItemWithUiEvents<ProtocolDrugSelectionItem.DrugViewHolder>(adapterId = id.toLong()) {

  init {
    if (option1 is DosageOption.Selected && option2 is DosageOption.Selected) {
      Timber.e(AssertionError("Both dosage options are selected for $drug"))
    }
  }

  override lateinit var uiEvents: Subject<UiEvent>

  override fun getLayout() = R.layout.list_prescribeddrugs_protocol_drug

  override fun createViewHolder(itemView: View): DrugViewHolder {
    val holder = DrugViewHolder(itemView)
    holder.dosage1RadioButton.setOnCheckedChangeListener { _, isChecked ->
      uiEvents.onNext(when {
        isChecked -> ProtocolDrugDosageSelected(drug, option1.dosage)
        else -> ProtocolDrugDosageUnselected(drug, (option1 as DosageOption.Selected).prescription)
      })
    }
    holder.dosage2RadioButton.setOnCheckedChangeListener { _, isChecked ->
      uiEvents.onNext(when {
        isChecked -> ProtocolDrugDosageSelected(drug, option2.dosage)
        else -> ProtocolDrugDosageUnselected(drug, (option2 as DosageOption.Selected).prescription)
      })
    }
    return holder
  }

  override fun bind(holder: DrugViewHolder, position: Int) {
    holder.nameTextView.text = drug.name
    holder.dosage1RadioButton.text = option1.dosage
    holder.dosage2RadioButton.text = option2.dosage
    holder.dosage1RadioButton.isChecked = option1 is DosageOption.Selected
    holder.dosage2RadioButton.isChecked = option2 is DosageOption.Selected
  }

  sealed class DosageOption {
    abstract val dosage: String

    data class Selected(override val dosage: String, val prescription: PrescribedDrug) : DosageOption()
    data class Unselected(override val dosage: String) : DosageOption()
  }

  class DrugViewHolder(rootView: View) : ViewHolder(rootView) {
    val nameTextView by bindView<TextView>(R.id.prescribeddrug_item_protocoldrug_name)
    val dosage1RadioButton by bindView<RadioButton>(R.id.prescribeddrug_item_protocoldrug_dosage_1)
    val dosage2RadioButton by bindView<RadioButton>(R.id.prescribeddrug_item_protocoldrug_dosage_2)
  }
}
