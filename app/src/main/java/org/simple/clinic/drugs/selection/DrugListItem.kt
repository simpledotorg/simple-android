package org.simple.clinic.drugs.selection

import androidx.recyclerview.widget.DiffUtil
import io.reactivex.subjects.Subject
import org.simple.clinic.R
import org.simple.clinic.databinding.ListPrescribeddrugsCustomDrugBinding
import org.simple.clinic.databinding.ListPrescribeddrugsNewCustomDrugBinding
import org.simple.clinic.databinding.ListPrescribeddrugsProtocolDrugBinding
import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.widgets.ItemAdapter
import org.simple.clinic.widgets.recyclerview.BindingViewHolder
import org.simple.clinic.widgets.visibleOrGone

sealed class DrugListItem(open val prescribedDrug: PrescribedDrug? = null) : ItemAdapter.Item<DrugListItemClicked> {

  class Differ : DiffUtil.ItemCallback<DrugListItem>() {
    override fun areItemsTheSame(oldItem: DrugListItem, newItem: DrugListItem): Boolean {
      return oldItem.prescribedDrug?.uuid == newItem.prescribedDrug?.uuid
    }

    override fun areContentsTheSame(oldItem: DrugListItem, newItem: DrugListItem): Boolean {
      return oldItem == newItem
    }
  }
}

data class ProtocolDrugListItem(
    val id: Int,
    val drugName: String,
    override val prescribedDrug: PrescribedDrug?
) : DrugListItem(prescribedDrug) {

  override fun layoutResId() = R.layout.list_prescribeddrugs_protocol_drug

  override fun render(holder: BindingViewHolder, subject: Subject<DrugListItemClicked>) {
    val viewBinding = holder.binding as ListPrescribeddrugsProtocolDrugBinding

    viewBinding.protocoldrugItemName.text = drugName
    viewBinding.protocoldrugItemName.isChecked = prescribedDrug != null

    viewBinding.protocoldrugItemDosage.visibleOrGone(prescribedDrug != null)
    viewBinding.protocoldrugItemDosage.text = prescribedDrug?.dosage

    viewBinding.root.setOnClickListener {
      subject.onNext(DrugListItemClicked.PrescribedDrugClicked(drugName, prescribedDrug))
    }
  }
}

data class CustomPrescribedDrugListItem(
    override val prescribedDrug: PrescribedDrug
) : DrugListItem(prescribedDrug) {

  override fun layoutResId() = R.layout.list_prescribeddrugs_custom_drug

  override fun render(holder: BindingViewHolder, subject: Subject<DrugListItemClicked>) {
    val viewBinding = holder.binding as ListPrescribeddrugsCustomDrugBinding

    viewBinding.prescribeddrugItemCustomdrugName.text = prescribedDrug.name
    viewBinding.prescribeddrugItemCustomdrugName.isChecked = true

    viewBinding.prescribeddrugItemCustomdrugDosage.text = prescribedDrug.dosage

    viewBinding.root.setOnClickListener {
      subject.onNext(DrugListItemClicked.CustomPrescriptionClicked(prescribedDrug))
    }
  }
}

object AddNewPrescriptionListItem : DrugListItem() {

  override fun layoutResId() = R.layout.list_prescribeddrugs_new_custom_drug

  override fun render(holder: BindingViewHolder, subject: Subject<DrugListItemClicked>) {
    val viewBinding = holder.binding as ListPrescribeddrugsNewCustomDrugBinding

    viewBinding.prescribeddrugItemAddnewprescription.setOnClickListener { subject.onNext(DrugListItemClicked.AddNewPrescriptionClicked) }
  }
}

sealed class DrugListItemClicked {
  data class PrescribedDrugClicked(val drugName: String, val prescribedDrug: PrescribedDrug?) : DrugListItemClicked()

  data class CustomPrescriptionClicked(val prescribedDrug: PrescribedDrug) : DrugListItemClicked()

  object AddNewPrescriptionClicked : DrugListItemClicked()
}
