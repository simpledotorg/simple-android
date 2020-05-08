package org.simple.clinic.drugs.selection.entry

import org.simple.clinic.mobius.ViewRenderer
import org.simple.clinic.util.ValueChangedCallback

class CustomPrescriptionEntryUiRenderer(private val ui: CustomPrescriptionEntryUi) : ViewRenderer<CustomPrescriptionEntryModel> {

  private val drugNameValueCallback = ValueChangedCallback<Boolean>()

  override fun render(model: CustomPrescriptionEntryModel) {
    initialSetup(model.openAs)
    showDefaultDosagePlaceholder(model.dosage, model.dosageHasFocus)
    toggleSaveButton(model.drugName)
  }

  private fun toggleSaveButton(drugName: String?) {
    if (drugName != null) {

      val isDrugNameNotBlank = drugName.isNotBlank()
      drugNameValueCallback.pass(isDrugNameNotBlank, ui::setSaveButtonEnabled)
    }
  }

  /**
   * The dosage field shows a default text as "mg". When it is focused, the cursor will
   * by default be moved to the end. This will force the user to either move the cursor
   * to the end manually or delete everything and essentially making the placeholder
   * useless. As a workaround, we move the cursor to the starting again.
   */
  private fun showDefaultDosagePlaceholder(dosage: String?, dosageHasFocus: Boolean?) {
    if (dosageHasFocus == null) return

    when {
      dosage != null && dosage.trim() == DOSAGE_PLACEHOLDER && dosageHasFocus.not() -> ui.setDrugDosageText("")    //reset
      dosage.isNullOrBlank() && dosageHasFocus -> {
        ui.setDrugDosageText(DOSAGE_PLACEHOLDER)
        ui.moveDrugDosageCursorToBeginning()
      }
    }
  }

  private fun initialSetup(openAs: OpenAs) {
    when (openAs) {
      is OpenAs.New -> setupUiForNewPrescription()
      is OpenAs.Update -> setupUiForEditingPrescription()
    }
  }

  private fun setupUiForEditingPrescription() {
    ui.showEditPrescriptionTitle()
    ui.showRemoveButton()
  }

  private fun setupUiForNewPrescription() {
    ui.showEnterNewPrescriptionTitle()
    ui.hideRemoveButton()
  }

  companion object {
    const val DOSAGE_PLACEHOLDER = "mg"
  }
}
