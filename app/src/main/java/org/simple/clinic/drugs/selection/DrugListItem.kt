package org.simple.clinic.drugs.selection

import androidx.recyclerview.widget.DiffUtil
import com.google.android.material.shape.ShapeAppearanceModel
import io.reactivex.subjects.Subject
import org.simple.clinic.R
import org.simple.clinic.databinding.ListPrescribeddrugsCustomDrugBinding
import org.simple.clinic.databinding.ListPrescribeddrugsNewCustomDrugBinding
import org.simple.clinic.databinding.ListPrescribeddrugsProtocolDrugBinding
import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.drugs.selection.custom.drugfrequency.country.DrugFrequencyLabel
import org.simple.clinic.teleconsultlog.medicinefrequency.MedicineFrequency
import org.simple.clinic.widgets.ItemAdapter
import org.simple.clinic.widgets.dp
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
    override val prescribedDrug: PrescribedDrug?,
    val hasTopCorners: Boolean,
    val medicineFrequencyToLabelMap: Map<MedicineFrequency?, DrugFrequencyLabel>
) : DrugListItem(prescribedDrug) {

  override fun layoutResId() = R.layout.list_prescribeddrugs_protocol_drug

  override fun render(holder: BindingViewHolder, subject: Subject<DrugListItemClicked>) {
    val viewBinding = holder.binding as ListPrescribeddrugsProtocolDrugBinding

    viewBinding.protocoldrugItemName.text = drugName
    viewBinding.protocoldrugItemName.isChecked = prescribedDrug != null

    viewBinding.protocoldrugItemDosageAndFrequency.visibleOrGone(prescribedDrug != null)
    val medicineFrequency = if (prescribedDrug?.frequency == null) null else medicineFrequencyToLabelMap[prescribedDrug.frequency]!!.label
    val dosageAndFrequency = listOfNotNull(prescribedDrug?.dosage, medicineFrequency).joinToString()
    viewBinding.protocoldrugItemDosageAndFrequency.text = dosageAndFrequency

    viewBinding.prescribeddrugItemProtocoldrugRootlayout.shapeAppearanceModel = shapeAppearanceModel(hasTopCorners)

    viewBinding.root.setOnClickListener {
      subject.onNext(DrugListItemClicked.PrescribedDrugClicked(drugName, prescribedDrug))
    }
  }

  fun withTopCorners(): ProtocolDrugListItem {
    return copy(hasTopCorners = true)
  }
}

data class CustomPrescribedDrugListItem(
    override val prescribedDrug: PrescribedDrug,
    val hasTopCorners: Boolean,
    val medicineFrequencyToLabelMap: Map<MedicineFrequency?, DrugFrequencyLabel>
) : DrugListItem(prescribedDrug) {

  override fun layoutResId() = R.layout.list_prescribeddrugs_custom_drug

  override fun render(holder: BindingViewHolder, subject: Subject<DrugListItemClicked>) {
    val viewBinding = holder.binding as ListPrescribeddrugsCustomDrugBinding

    viewBinding.prescribeddrugItemCustomdrugName.text = prescribedDrug.name
    viewBinding.prescribeddrugItemCustomdrugName.isChecked = true

    val medicineFrequency = if (prescribedDrug.frequency == null) null else medicineFrequencyToLabelMap[prescribedDrug.frequency]!!.label
    val dosageAndFrequency = listOfNotNull(prescribedDrug.dosage, medicineFrequency).joinToString()
    viewBinding.protocoldrugItemDosageAndFrequency.text = dosageAndFrequency

    viewBinding.root.setOnClickListener {
      subject.onNext(DrugListItemClicked.CustomPrescriptionClicked(prescribedDrug))
    }

    viewBinding.prescribeddrugItemCustomdrugRootlayout.shapeAppearanceModel = shapeAppearanceModel(hasTopCorners)
  }

  fun withTopCorners(): CustomPrescribedDrugListItem {
    return copy(hasTopCorners = true)
  }
}

object AddNewPrescriptionListItem : DrugListItem() {

  override fun layoutResId() = R.layout.list_prescribeddrugs_new_custom_drug

  override fun render(holder: BindingViewHolder, subject: Subject<DrugListItemClicked>) {
    val viewBinding = holder.binding as ListPrescribeddrugsNewCustomDrugBinding

    viewBinding.prescribeddrugItemAddnewprescription.setOnClickListener { subject.onNext(DrugListItemClicked.AddNewPrescriptionClicked) }
  }
}

private fun shapeAppearanceModel(hasTopCorners: Boolean) = if (hasTopCorners) {
  ShapeAppearanceModel
      .builder()
      .setTopLeftCornerSize(4.dp.toFloat())
      .setTopRightCornerSize(4.dp.toFloat())
      .build()
} else {
  ShapeAppearanceModel
      .builder()
      .build()
}

sealed class DrugListItemClicked {
  data class PrescribedDrugClicked(
      val drugName: String,
      val prescribedDrug: PrescribedDrug?
  ) : DrugListItemClicked()

  data class CustomPrescriptionClicked(val prescribedDrug: PrescribedDrug) : DrugListItemClicked()

  object AddNewPrescriptionClicked : DrugListItemClicked()
}
