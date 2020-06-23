package org.simple.clinic.drugs.selection.entry

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.drugs.PrescribedDrug
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
      is CustomPrescriptionFetched -> prescriptionFetched(event.prescription)
      RemoveCustomPrescriptionClicked -> {
        val update = model.openAs as OpenAs.Update
        dispatch(ShowConfirmRemoveMedicineDialog(update.prescribedDrugUuid))
      }
    }
  }

  private fun prescriptionFetched(
      prescription: PrescribedDrug
  ): Next<CustomPrescriptionEntryModel, CustomPrescriptionEntryEffect> {
    return if (prescription.isDeleted) {
      dispatch(CloseSheet)
    } else {
      dispatch(SetMedicineName(prescription.name), SetDosage(prescription.dosage))
    }
  }

  private fun createOrUpdatePrescriptionEntry(model: CustomPrescriptionEntryModel): Next<CustomPrescriptionEntryModel, CustomPrescriptionEntryEffect> {
    return when (val openAs = model.openAs) {
      is OpenAs.New -> dispatch(SaveCustomPrescription(openAs.patientUuid, model.drugName!!, model.dosage))
      is OpenAs.Update -> dispatch(UpdatePrescription(openAs.patientUuid, openAs.prescribedDrugUuid, model.drugName!!, model.dosage))
    }
  }
}
