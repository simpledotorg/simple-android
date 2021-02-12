package org.simple.clinic.drugs.selection.dosage

import android.annotation.SuppressLint
import androidx.recyclerview.widget.DiffUtil
import io.reactivex.subjects.Subject
import org.simple.clinic.R
import org.simple.clinic.databinding.PrescribedDrugWithDosageListItemBinding
import org.simple.clinic.drugs.selection.dosage.DosageListItem.WithDosage
import org.simple.clinic.drugs.selection.dosage.DosageListItem.WithoutDosage
import org.simple.clinic.protocol.ProtocolDrug
import org.simple.clinic.router.util.resolveColor
import org.simple.clinic.widgets.ItemAdapter
import org.simple.clinic.widgets.recyclerview.BindingViewHolder

sealed class DosageListItem : ItemAdapter.Item<DosageListItem.Event> {

  companion object {
    fun from(protocolDrugs: List<ProtocolDrug>, hasExistingPrescription: Boolean): List<DosageListItem> {
      return protocolDrugs
          .map(::WithDosage)
          .plus(WithoutDosage(hasExistingPrescription))
    }
  }

  data class WithDosage(val protocolDrug: ProtocolDrug) : DosageListItem() {

    override fun layoutResId() = R.layout.prescribed_drug_with_dosage_list_item

    override fun render(holder: BindingViewHolder, subject: Subject<Event>) {
      val context = holder.itemView.context
      val binding = holder.binding as PrescribedDrugWithDosageListItemBinding

      binding.dosageTextView.text = protocolDrug.dosage
      binding.dosageTextView.setTextColor(context.resolveColor(colorRes = R.color.color_on_surface_67))

      holder.itemView.setOnClickListener { subject.onNext(Event.DosageClicked(protocolDrug)) }
    }
  }

  data class WithoutDosage(val hasExistingPrescription: Boolean) : DosageListItem() {

    override fun layoutResId() = R.layout.prescribed_drug_with_dosage_list_item

    override fun render(holder: BindingViewHolder, subject: Subject<Event>) {
      val context = holder.itemView.context
      val binding = holder.binding as PrescribedDrugWithDosageListItemBinding

      if (hasExistingPrescription) {
        binding.dosageTextView.setTextColor(context.resolveColor(attrRes = R.attr.colorError))
        binding.dosageTextView.text = holder.itemView.context.getString(R.string.prescribed_drugs_dosage_remove)
      } else {
        binding.dosageTextView.setTextColor(context.resolveColor(colorRes = R.color.color_on_surface_67))
        binding.dosageTextView.text = holder.itemView.context.getString(R.string.prescribed_drugs_dosage_none)
      }

      holder.itemView.setOnClickListener { subject.onNext(Event.NoneClicked) }
    }
  }

  sealed class Event {
    object NoneClicked : Event()
    data class DosageClicked(val protocolDrug: ProtocolDrug) : Event()
  }
}

class DosageDiffer : DiffUtil.ItemCallback<DosageListItem>() {
  override fun areItemsTheSame(oldItem: DosageListItem, newItem: DosageListItem): Boolean {
    return when {
      oldItem is WithDosage && newItem is WithDosage -> oldItem.protocolDrug.uuid == newItem.protocolDrug.uuid
      oldItem is WithoutDosage && newItem is WithoutDosage -> true
      else -> false
    }
  }

  @SuppressLint("DiffUtilEquals")
  override fun areContentsTheSame(oldItem: DosageListItem, newItem: DosageListItem): Boolean {
    return oldItem == newItem
  }
}
