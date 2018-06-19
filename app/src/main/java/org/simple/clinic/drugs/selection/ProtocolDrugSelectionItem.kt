package org.simple.clinic.drugs.selection

import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.CompoundButton
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
import org.simple.clinic.widgets.setCompoundDrawableStart
import org.simple.clinic.widgets.setPadding

data class ProtocolDrugSelectionItem constructor(
    val id: Int,
    val drug: ProtocolDrug,
    val option1: DosageOption,
    val option2: DosageOption
) : GroupieItemWithUiEvents<ProtocolDrugSelectionItem.DrugViewHolder>(adapterId = id.toLong()) {

  override lateinit var uiEvents: Subject<UiEvent>

  private val dosage1CheckedChangeListener: (CompoundButton, Boolean) -> Unit = { _, isChecked ->
    uiEvents.onNext(when {
      isChecked -> ProtocolDrugDosageSelected(drug, option1.dosage)
      else -> ProtocolDrugDosageUnselected(drug, (option1 as DosageOption.Selected).prescription)
    })
  }

  private val dosage2CheckedChangeListener: (CompoundButton, Boolean) -> Unit = { _, isChecked ->
    uiEvents.onNext(when {
      isChecked -> ProtocolDrugDosageSelected(drug, option2.dosage)
      else -> ProtocolDrugDosageUnselected(drug, (option2 as DosageOption.Selected).prescription)
    })
  }

  override fun getLayout() = R.layout.list_prescribeddrugs_protocol_drug

  override fun createViewHolder(itemView: View): DrugViewHolder {
    return DrugViewHolder(itemView)
  }

  override fun bind(holder: DrugViewHolder, position: Int) {
    holder.nameTextView.text = drug.name
    holder.dosage1RadioButton.text = option1.dosage
    holder.dosage2RadioButton.text = option2.dosage

    holder.dosage1RadioButton.setOnCheckedChangeListener(null)
    holder.dosage2RadioButton.setOnCheckedChangeListener(null)

    holder.dosage1RadioButton.isChecked = option1 is DosageOption.Selected
    holder.dosage2RadioButton.isChecked = option2 is DosageOption.Selected

    holder.dosage1RadioButton.setOnCheckedChangeListener(dosage1CheckedChangeListener)
    holder.dosage2RadioButton.setOnCheckedChangeListener(dosage2CheckedChangeListener)

    val color: (Int) -> Int = { colorRes -> ContextCompat.getColor(holder.itemView.context, colorRes) }
    val setCheckedState: (RadioButton) -> Unit = {
      it.apply {
        when {
          isChecked -> {
            setTextColor(color(R.color.prescribeddrugs_protocol_drug_selected))
            setCompoundDrawableStart(R.drawable.ic_done_16dp)
            setPadding(R.dimen.prescribeddrugs_protocol_drug_selected_padding)
          }
          else -> {
            setTextColor(color(R.color.prescribeddrugs_protocol_drug_unselected))
            setCompoundDrawableStart(null)
            setPadding(R.dimen.prescribeddrugs_protocol_drug_unselected_padding)
          }
        }
      }
    }
    setCheckedState(holder.dosage1RadioButton)
    setCheckedState(holder.dosage2RadioButton)
  }

  sealed class DosageOption {
    abstract val dosage: String

    /**
     * [prescription] is required for soft-deleting it later.
     */
    data class Selected(override val dosage: String, val prescription: PrescribedDrug) : DosageOption()

    data class Unselected(override val dosage: String) : DosageOption()
  }

  class DrugViewHolder(rootView: View) : ViewHolder(rootView) {
    val nameTextView by bindView<TextView>(R.id.prescribeddrug_item_protocoldrug_name)
    val dosage1RadioButton by bindView<RadioButton>(R.id.prescribeddrug_item_protocoldrug_dosage_1)
    val dosage2RadioButton by bindView<RadioButton>(R.id.prescribeddrug_item_protocoldrug_dosage_2)
  }
}
