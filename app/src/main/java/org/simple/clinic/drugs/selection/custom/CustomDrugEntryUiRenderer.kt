package org.simple.clinic.drugs.selection.custom

import org.simple.clinic.drugs.search.DrugFrequency
import org.simple.clinic.mobius.ViewRenderer
import org.simple.clinic.util.nullIfBlank

class CustomDrugEntryUiRenderer(
    private val ui: CustomDrugEntryUi,
    private val dosagePlaceholder: String
) : ViewRenderer<CustomDrugEntryModel> {
  override fun render(model: CustomDrugEntryModel) {
    initialSetup(model.openAs)

    setSheetTitle(model.drugName, model.dosage, model.frequency)

    showDefaultDosagePlaceholder(model.dosage, model.dosageHasFocus)
  }

  private fun setSheetTitle(drugName: String?, dosage: String?, frequency: DrugFrequency?) {
    val sheetTitle = listOfNotNull(drugName, dosage.nullIfBlank(), frequency?.toString()).joinToString()
    ui.setSheetTitle(sheetTitle)
  }

  private fun showDefaultDosagePlaceholder(
      dosage: String?,
      dosageHasFocus: Boolean?
  ) {
    if (dosageHasFocus == null) return

    when {
      dosage != null && dosage == dosagePlaceholder && dosageHasFocus.not() -> ui.setDrugDosageText("")
      dosage.isNullOrBlank() && dosageHasFocus -> {
        ui.setDrugDosageText(dosagePlaceholder)
        ui.moveDrugDosageCursorToBeginning()
      }
    }
  }

  private fun initialSetup(openAs: OpenAs) {
    when (openAs) {
      is OpenAs.New -> setUpUIForCreatingDrugEntry()
      is OpenAs.Update -> setUpUIForUpdatingDrugEntry()
    }
  }

  private fun setUpUIForCreatingDrugEntry() {
    ui.hideRemoveButton()
    ui.setButtonTextAsAdd()
  }

  private fun setUpUIForUpdatingDrugEntry() {
    ui.showRemoveButton()
    ui.setButtonTextAsSave()
  }
}
