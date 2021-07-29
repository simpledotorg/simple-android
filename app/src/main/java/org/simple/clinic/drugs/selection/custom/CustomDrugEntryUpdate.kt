package org.simple.clinic.drugs.selection.custom

import com.spotify.mobius.Next
import com.spotify.mobius.Next.next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next

class CustomDrugEntryUpdate : Update<CustomDrugEntryModel, CustomDrugEntryEvent, CustomDrugEntryEffect> {
  override fun update(
      model: CustomDrugEntryModel,
      event: CustomDrugEntryEvent
  ): Next<CustomDrugEntryModel, CustomDrugEntryEffect> {
    return when (event) {
      is DosageEdited -> next(model.dosageEdited(event.dosage), SetSheetTitle(model.drugName, event.dosage, model.frequency))
      is DosageFocusChanged -> next(model.dosageFocusChanged(event.hasFocus))
      is EditFrequencyClicked -> dispatch(ShowEditFrequencyDialog(event.frequency))
      is FrequencyEdited -> next(model.frequencyEdited(event.frequency), SetDrugFrequency(event.frequency), SetSheetTitle(model.drugName, model.dosage, event.frequency))
      is AddMedicineButtonClicked -> createOrUpdatePrescriptionEntry(model)
      is CustomDrugSaved -> dispatch(CloseBottomSheet)
    }
  }

  private fun createOrUpdatePrescriptionEntry(model: CustomDrugEntryModel): Next<CustomDrugEntryModel, CustomDrugEntryEffect> {
    return when (model.openAs) {
      is OpenAs.New.FromDrugList -> dispatch(SaveCustomDrugToPrescription(model.openAs.patientUuid, model.openAs.drug.name, model.dosage, model.openAs.drug.rxNormCode, model.frequency))
      is OpenAs.New.FromDrugName -> dispatch(SaveCustomDrugToPrescription(model.openAs.patientUuid, model.openAs.drugName, model.dosage, null, model.frequency))
      is OpenAs.Update -> dispatch(UpdatePrescription(model.openAs.patientUuid, model.openAs.prescribedDrugUuid, model.drugName!!, model.dosage, model.rxNormCode, model.frequency))
    }
  }
}
