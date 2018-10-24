package org.simple.clinic.drugs.selection

import android.view.View
import android.widget.CompoundButton
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
import org.simple.clinic.widgets.setTextColorResource

data class ProtocolDrugSelectionListItem(
    val id: Int,
    val drug: ProtocolDrug,
    val option1: DosageOption,
    val option2: DosageOption
) : GroupieItemWithUiEvents<ProtocolDrugSelectionListItem.DrugViewHolder>(adapterId = id.toLong()) {

  override lateinit var uiEvents: Subject<UiEvent>

  // TODO.
  // Warning: very ugly way of saving and deleting prescriptions.
  // We should find out a better of doing this post-MVP launch.
  private val dosage1CheckedChangeListener: (CompoundButton, Boolean) -> Unit = { _, isChecked ->
    when {
      isChecked -> {
        uiEvents.onNext(ProtocolDrugDosageSelected(drug, option1.dosage))
        if (option2 is DosageOption.Selected) {
          uiEvents.onNext(ProtocolDrugDosageUnselected(drug, option2.prescription))
        }
      }
      else -> {
        uiEvents.onNext(ProtocolDrugDosageUnselected(drug, (option1 as DosageOption.Selected).prescription))
      }
    }
  }

  private val dosage2CheckedChangeListener: (CompoundButton, Boolean) -> Unit = { _, isChecked ->
    when {
      isChecked -> {
        uiEvents.onNext(ProtocolDrugDosageSelected(drug, option2.dosage))
        if (option1 is DosageOption.Selected) {
          uiEvents.onNext(ProtocolDrugDosageUnselected(drug, option1.prescription))
        }
      }
      else -> {
        uiEvents.onNext(ProtocolDrugDosageUnselected(drug, (option2 as DosageOption.Selected).prescription))
      }
    }
  }

  override fun getLayout() = R.layout.list_prescribeddrugs_protocol_drug

  override fun createViewHolder(itemView: View): DrugViewHolder {
    return DrugViewHolder(itemView)
  }

  override fun bind(holder: DrugViewHolder, position: Int) {
    holder.nameTextView.text = drug.name
    holder.dosage1Button.text = option1.dosage
    holder.dosage2Button.text = option2.dosage

    holder.dosage1Button.setOnCheckedChangeListener(null)
    holder.dosage2Button.setOnCheckedChangeListener(null)

    holder.dosage1Button.isChecked = option1 is DosageOption.Selected
    holder.dosage2Button.isChecked = option2 is DosageOption.Selected

    holder.dosage1Button.setOnCheckedChangeListener(dosage1CheckedChangeListener)
    holder.dosage2Button.setOnCheckedChangeListener(dosage2CheckedChangeListener)

    val setCheckedState: (CompoundButton) -> Unit = {
      it.apply {
        when {
          isChecked -> {
            setTextColorResource(R.color.prescribeddrugs_protocol_drug_selected)
            setCompoundDrawableStart(R.drawable.ic_done_16dp)
            setPadding(R.dimen.prescribeddrugs_protocol_drug_selected_padding)
          }
          else -> {
            setTextColorResource(R.color.prescribeddrugs_protocol_drug_unselected)
            setCompoundDrawableStart(null)
            setPadding(R.dimen.prescribeddrugs_protocol_drug_unselected_padding)
          }
        }
      }
    }
    setCheckedState(holder.dosage1Button)
    setCheckedState(holder.dosage2Button)
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
    val dosage1Button by bindView<CompoundButton>(R.id.prescribeddrug_item_protocoldrug_dosage_1)
    val dosage2Button by bindView<CompoundButton>(R.id.prescribeddrug_item_protocoldrug_dosage_2)
  }
}
