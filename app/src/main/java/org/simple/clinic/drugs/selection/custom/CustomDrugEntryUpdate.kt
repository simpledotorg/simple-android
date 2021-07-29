package org.simple.clinic.drugs.selection.custom

import com.spotify.mobius.Next
import com.spotify.mobius.Next.next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch

class CustomDrugEntryUpdate : Update<CustomDrugEntryModel, CustomDrugEntryEvent, CustomDrugEntryEffect> {
  override fun update(
      model: CustomDrugEntryModel,
      event: CustomDrugEntryEvent
  ): Next<CustomDrugEntryModel, CustomDrugEntryEffect> {
    return when (event) {
      is DosageEdited -> next(model.dosageEdited(event.dosage))
      is EditFrequencyClicked -> dispatch(ShowEditFrequencyDialog(event.frequency))
      is FrequencyEdited -> next(model.frequencyEdited(event.frequency))
      is AddMedicineButtonClicked -> createOrUpdatePrescriptionEntry(model)
      is CustomDrugSaved -> noChange()
    }
  }

  private fun createOrUpdatePrescriptionEntry(model: CustomDrugEntryModel): Next<CustomDrugEntryModel, CustomDrugEntryEffect> {
    return when (model.openAs) {
      is OpenAs.New -> dispatch(SaveCustomDrugToPrescription(model.openAs.patientUuid, model.drugName, model.dosage, model.rxNormCode, model.frequency))
      is OpenAs.Update -> noChange()
    }
  }
}
