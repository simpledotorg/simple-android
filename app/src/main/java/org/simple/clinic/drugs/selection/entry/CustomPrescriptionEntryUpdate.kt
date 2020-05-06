package org.simple.clinic.drugs.selection.entry

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next

class CustomPrescriptionEntryUpdate : Update<CustomPrescriptionEntryModel, CustomPrescriptionEntryEvent, CustomPrescriptionEntryEffect> {

  override fun update(model: CustomPrescriptionEntryModel, event: CustomPrescriptionEntryEvent):
      Next<CustomPrescriptionEntryModel, CustomPrescriptionEntryEffect> {
    return when (event) {
      is CustomPrescriptionDrugNameTextChanged -> next(model.drugNameChanged(event.name))
      is CustomPrescriptionDrugDosageTextChanged -> next(model.dosageChanged(event.dosage))
      is CustomPrescriptionDrugDosageFocusChanged -> next(model.dosageFocusChanged(event.hasFocus))
      SaveCustomPrescriptionClicked -> createOrUpdatePrescriptionEntry(model)
      CustomPrescriptionSaved -> dispatch(CloseSheet)
      is CustomPrescriptionFetched -> TODO()
    }
  }

  private fun createOrUpdatePrescriptionEntry(model: CustomPrescriptionEntryModel): Next<CustomPrescriptionEntryModel, CustomPrescriptionEntryEffect> {
    return when (model.openAs) {
      is OpenAs.New -> dispatch(SaveCustomPrescription(model.openAs.patientUuid, model.drugName!!, model.dosage))
      is OpenAs.Update -> dispatch(UpdatePrescription(model.openAs.prescribedDrugUuid, model.drugName!!, model.dosage))
    }
  }
}
