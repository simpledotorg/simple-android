package org.simple.clinic.drugs.selection.custom

import com.spotify.mobius.Next
import com.spotify.mobius.Next.next
import com.spotify.mobius.Update
import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.drugs.search.Drug
import org.simple.clinic.drugs.search.DrugFrequency
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
      is CustomDrugSaved, ExistingDrugRemoved -> dispatch(CloseBottomSheet)
      is PrescribedDrugFetched -> prescriptionFetched(model, event.prescription)
      is RemoveDrugButtonClicked -> {
        val update = model.openAs as OpenAs.Update
        dispatch(RemoveDrugFromPrescription(update.prescribedDrugUuid))
      }
      is DrugFetched -> drugFetched(model, event.drug)
    }
  }

  private fun drugFetched(
      model: CustomDrugEntryModel,
      drug: Drug
  ): Next<CustomDrugEntryModel, CustomDrugEntryEffect> {
    val updatedModel = model.drugNameLoaded(drug.name).dosageEdited(drug.dosage).frequencyEdited(drug.frequency).rxNormCodeEdited(drug.rxNormCode)

    return next(updatedModel, SetSheetTitle(drug.name, drug.dosage, drug.frequency), SetDrugFrequency(drug.frequency), SetDrugDosage(drug.dosage))
  }

  private fun prescriptionFetched(
      model: CustomDrugEntryModel,
      prescription: PrescribedDrug
  ): Next<CustomDrugEntryModel, CustomDrugEntryEffect> {
    val frequency = DrugFrequency.fromMedicineFrequency(prescription.frequency)

    val updatedModel = model.drugNameLoaded(prescription.name).dosageEdited(prescription.dosage).frequencyEdited(frequency).rxNormCodeEdited(prescription.rxNormCode)

    return next(updatedModel, SetSheetTitle(prescription.name, prescription.dosage, frequency), SetDrugFrequency(frequency))
  }

  private fun createOrUpdatePrescriptionEntry(model: CustomDrugEntryModel): Next<CustomDrugEntryModel, CustomDrugEntryEffect> {
    return when (model.openAs) {
      is OpenAs.New.FromDrugList -> dispatch(SaveCustomDrugToPrescription(model.openAs.patientUuid, model.drugName!!, model.dosage, model.rxNormCode, model.frequency))
      is OpenAs.New.FromDrugName -> dispatch(SaveCustomDrugToPrescription(model.openAs.patientUuid, model.openAs.drugName, model.dosage, null, model.frequency))
      is OpenAs.Update -> dispatch(UpdatePrescription(model.openAs.patientUuid, model.openAs.prescribedDrugUuid, model.drugName!!, model.dosage, model.rxNormCode, model.frequency))
    }
  }
}
