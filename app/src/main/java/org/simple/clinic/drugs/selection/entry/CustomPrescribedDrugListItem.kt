package org.simple.clinic.drugs.selection.entry

import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import io.reactivex.subjects.Subject
import org.simple.clinic.R
import org.simple.clinic.databinding.ListPrescribeddrugsCustomDrugBinding
import org.simple.clinic.drugs.CustomPrescriptionClicked
import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.summary.GroupieItemWithUiEvents
import org.simple.clinic.widgets.UiEvent

data class CustomPrescribedDrugListItem(
    val prescription: PrescribedDrug,
    val hideDivider: Boolean
) : GroupieItemWithUiEvents<ListPrescribeddrugsCustomDrugBinding>(prescription.hashCode().toLong()) {

  override lateinit var uiEvents: Subject<UiEvent>

  override fun getLayout() = R.layout.list_prescribeddrugs_custom_drug

  override fun initializeViewBinding(view: View): ListPrescribeddrugsCustomDrugBinding {
    return ListPrescribeddrugsCustomDrugBinding.bind(view)
  }

  override fun bind(viewBinding: ListPrescribeddrugsCustomDrugBinding, position: Int) {
    viewBinding.prescribeddrugItemCustomdrugName.text = prescription.name
    viewBinding.prescribeddrugItemCustomdrugName.isChecked = true

    viewBinding.prescribeddrugItemCustomdrugDosage.text = prescription.dosage
    viewBinding.protocoldrugItemDivider.visibility = if (hideDivider) GONE else VISIBLE

    viewBinding.root.setOnClickListener {
      uiEvents.onNext(CustomPrescriptionClicked(prescription))
    }
  }
}
