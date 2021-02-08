package org.simple.clinic.drugs.selection

import android.view.View
import io.reactivex.subjects.Subject
import org.simple.clinic.R
import org.simple.clinic.databinding.ListPrescribeddrugsNewCustomDrugBinding
import org.simple.clinic.drugs.AddNewPrescriptionClicked
import org.simple.clinic.summary.GroupieItemWithUiEvents
import org.simple.clinic.summary.SummaryListAdapterIds.ADD_PRESCRIPTION
import org.simple.clinic.widgets.UiEvent

class AddNewPrescriptionListItem : GroupieItemWithUiEvents<ListPrescribeddrugsNewCustomDrugBinding>(adapterId = ADD_PRESCRIPTION) {

  override lateinit var uiEvents: Subject<UiEvent>

  override fun getLayout() = R.layout.list_prescribeddrugs_new_custom_drug

  override fun initializeViewBinding(view: View): ListPrescribeddrugsNewCustomDrugBinding {
    return ListPrescribeddrugsNewCustomDrugBinding.bind(view)
  }

  override fun bind(viewBinding: ListPrescribeddrugsNewCustomDrugBinding, position: Int) {
    viewBinding.prescribeddrugItemAddnewprescription.setOnClickListener { uiEvents.onNext(AddNewPrescriptionClicked) }
  }
}
