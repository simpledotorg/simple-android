package org.simple.clinic.drugs.selection.custom

import com.spotify.mobius.Next
import com.spotify.mobius.Next.next
import com.spotify.mobius.Update
import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.drugs.search.Drug
import org.simple.clinic.drugs.search.DrugFrequency
import org.simple.clinic.drugs.selection.custom.ButtonState.SAVING
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next
import java.util.UUID

class CustomDrugEntryUpdate : Update<CustomDrugEntryModel, CustomDrugEntryEvent, CustomDrugEntryEffect> {
  override fun update(
      model: CustomDrugEntryModel,
      event: CustomDrugEntryEvent
  ): Next<CustomDrugEntryModel, CustomDrugEntryEffect> {
    return when (event) {
      is DosageEdited -> next(model.dosageEdited(event.dosage))
      is DosageFocusChanged -> next(model.dosageFocusChanged(event.hasFocus))
      is EditFrequencyClicked -> dispatch(ShowEditFrequencyDialog(model.frequency), ClearFocusFromDosageEditText)
      is FrequencyEdited -> next(model.frequencyEdited(event.frequency), SetDrugFrequency(model.drugFrequencyToLabelMap!![event.frequency]!!.label))
      is AddMedicineButtonClicked -> createOrUpdatePrescriptionEntry(model, event.patientUuid)
      is CustomDrugSaved, ExistingDrugRemoved -> dispatch(CloseSheetAndGoToEditMedicineScreen)
      is PrescribedDrugFetched -> prescriptionFetched(model, event.prescription)
      is RemoveDrugButtonClicked -> {
        val update = model.openAs as OpenAs.Update
        next(model.drugInfoProgressStateLoading(), RemoveDrugFromPrescription(update.prescribedDrugUuid))
      }
      is DrugFetched -> drugFetched(model, event.drug)
      is DrugFrequencyChoiceItemsLoaded -> drugFrequencyChoiceItemsLoaded(model, event)
      ImeActionDoneClicked -> dispatch(HideKeyboard)
    }
  }

  private fun drugFrequencyChoiceItemsLoaded(
      model: CustomDrugEntryModel,
      event: DrugFrequencyChoiceItemsLoaded
  ): Next<CustomDrugEntryModel, CustomDrugEntryEffect> {
    return next(model.drugFrequencyToLabelMapLoaded(event.drugFrequencyToLabelMap))
  }

  private fun drugFetched(
      model: CustomDrugEntryModel,
      drug: Drug
  ): Next<CustomDrugEntryModel, CustomDrugEntryEffect> {
    val cursorPosition = cursorPositionFromDosage(drug.dosage)

    val updatedModel = model
        .drugNameLoaded(drug.name)
        .dosageEdited(drug.dosage)
        .frequencyEdited(drug.frequency)
        .rxNormCodeEdited(drug.rxNormCode)
        .drugInfoProgressStateLoaded()

    return next(updatedModel, SetDrugFrequency(model.drugFrequencyToLabelMap!![drug.frequency]!!.label), SetDrugDosage(drug.dosage), ShowKeyboard, SetCursorPosition(cursorPosition))
  }

  private fun cursorPositionFromDosage(dosage: String?): Int {
    if (dosage.isNullOrEmpty()) return 0
    val filteredDigitList = dosage.filter { it.isDigit() }
    return if (filteredDigitList.isNotEmpty()) dosage.lastIndexOf(filteredDigitList.last()) + 1 else 0
  }

  private fun prescriptionFetched(
      model: CustomDrugEntryModel,
      prescription: PrescribedDrug
  ): Next<CustomDrugEntryModel, CustomDrugEntryEffect> {
    val frequency = DrugFrequency.fromMedicineFrequency(prescription.frequency)
    val cursorPosition = cursorPositionFromDosage(prescription.dosage)

    val updatedModel = model
        .drugNameLoaded(prescription.name)
        .dosageEdited(prescription.dosage)
        .frequencyEdited(frequency)
        .rxNormCodeEdited(prescription.rxNormCode)
        .drugInfoProgressStateLoaded()

    return next(updatedModel, SetDrugFrequency(model.drugFrequencyToLabelMap!![frequency]!!.label), SetDrugDosage(prescription.dosage), ShowKeyboard, SetCursorPosition(cursorPosition))
  }

  private fun createOrUpdatePrescriptionEntry(
      model: CustomDrugEntryModel,
      patientUuid: UUID
  ): Next<CustomDrugEntryModel, CustomDrugEntryEffect> {
    val isAValidDosage = model.dosage != null && model.dosage.filter { it.isDigit() }.isNotBlank()
    val dosage = if (isAValidDosage) model.dosage else null

    val effect = when (model.openAs) {
      is OpenAs.New.FromDrugList -> SaveCustomDrugToPrescription(patientUuid, model.drugName!!, dosage, model.rxNormCode, model.frequency)
      is OpenAs.New.FromDrugName -> SaveCustomDrugToPrescription(patientUuid, model.openAs.drugName, dosage, null, model.frequency)
      is OpenAs.Update -> UpdatePrescription(patientUuid, model.openAs.prescribedDrugUuid, model.drugName!!, dosage, model.rxNormCode, model.frequency)
    }

    return next(model.saveButtonStateChanged(SAVING), effect)
  }
}
