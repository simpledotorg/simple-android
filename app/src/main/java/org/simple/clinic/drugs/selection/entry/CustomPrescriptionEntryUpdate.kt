package org.simple.clinic.drugs.selection.entry

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update
import org.simple.clinic.drugs.selection.entry.OpenAs.New
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next

class CustomPrescriptionEntryUpdate : Update<CustomPrescriptionEntryModel, CustomPrescriptionEntryEvent, CustomPrescriptionEntryEffect> {

  override fun update(model: CustomPrescriptionEntryModel, event: CustomPrescriptionEntryEvent):
      Next<CustomPrescriptionEntryModel, CustomPrescriptionEntryEffect> {
    return when (event) {
      is CustomPrescriptionDrugNameTextChanged -> next(model.drugNameChanged(event.name))
      is CustomPrescriptionDrugDosageTextChanged -> next(model.dosageChanged(event.dosage))
      SaveCustomPrescriptionClicked -> createNewPrescriptionEntry(model)
      CustomPrescriptionSaved -> dispatch(CloseSheet)
    }
  }

  private fun createNewPrescriptionEntry(model: CustomPrescriptionEntryModel): Next<CustomPrescriptionEntryModel, CustomPrescriptionEntryEffect> {
    return when (model.openAs) {
      is New -> dispatch(SaveCustomPrescription(model.openAs.patientUuid, model.drugName!!, model.dosage))
      else -> noChange()
    }
  }
}
