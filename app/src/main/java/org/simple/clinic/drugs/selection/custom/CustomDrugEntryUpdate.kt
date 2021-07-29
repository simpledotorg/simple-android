package org.simple.clinic.drugs.selection.custom

import com.spotify.mobius.Next
import com.spotify.mobius.Next.next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update
import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.drugs.search.DrugFrequency
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
      is CustomDrugSaved -> dispatch(CloseBottomSheet)
      is CustomDrugFetched -> drugFetched(model, event.prescription)
    }
  }

  private fun drugFetched(
      model: CustomDrugEntryModel,
      prescription: PrescribedDrug
  ): Next<CustomDrugEntryModel, CustomDrugEntryEffect> {
    return if (prescription.isDeleted) {
      dispatch(CloseBottomSheet)
    } else {
      next(model.dosageEdited(prescription.dosage).frequencyEdited(DrugFrequency.fromMedicineFrequencyToDrugFrequency(prescription.frequency)))
    }
  }

  private fun createOrUpdatePrescriptionEntry(model: CustomDrugEntryModel): Next<CustomDrugEntryModel, CustomDrugEntryEffect> {
    return when (model.openAs) {
      is OpenAs.New -> dispatch(SaveCustomDrugToPrescription(model.openAs.patientUuid, model.drugName, model.dosage, model.rxNormCode, model.frequency))
      is OpenAs.Update -> dispatch(UpdatePrescription(model.openAs.patientUuid, model.openAs.prescribedDrugUuid, model.drugName, model.dosage, model.rxNormCode, model.frequency))
    }
  }
}
