package org.simple.clinic.drugs.selection.entry

import org.simple.clinic.mobius.ViewRenderer
import org.simple.clinic.util.ValueChangedCallback

class CustomPrescriptionEntryUiRenderer(val ui: CustomPrescriptionEntryUi) : ViewRenderer<CustomPrescriptionEntryModel> {

  private val drugNameValueCallback = ValueChangedCallback<Boolean>()

  override fun render(model: CustomPrescriptionEntryModel) {
    showDefaultDosagePlaceholder(model.dosage, model.dosageHasFocus)

    if (model.drugName == null) return

    val isDrugNameNotBlank = model.drugName.isNotBlank()
    drugNameValueCallback.pass(isDrugNameNotBlank, ui::setSaveButtonEnabled)
    displaySheetTitle(model.openAs)

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

  private fun displaySheetTitle(openAs: OpenAs) {
    when (openAs) {
      is OpenAs.New -> ui.showEnterNewPrescriptionTitle()
      is OpenAs.Update -> ui.showEditPrescriptionTitle()
    }
  }
}
