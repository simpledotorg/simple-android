package org.simple.clinic.drugs.selection.custom

import org.simple.clinic.mobius.ViewRenderer

class CustomDrugEntryUiRenderer(
    private val ui: CustomDrugEntryUi,
    private val dosagePlaceholder: String
) : ViewRenderer<CustomDrugEntryModel> {
  override fun render(model: CustomDrugEntryModel) {
    showDefaultDosagePlaceholder(model.dosage, model.dosageHasFocus)
  }

  private fun showDefaultDosagePlaceholder(dosage: String?, dosageHasFocus: Boolean?) {
    if (dosageHasFocus == null) return

    when {
      dosage != null && dosage == dosagePlaceholder && dosageHasFocus.not() -> ui.setDrugDosageText("")
      dosage.isNullOrBlank() && dosageHasFocus -> {
        ui.setDrugDosageText(dosagePlaceholder)
        ui.moveDrugDosageCursorToBeginning()
      }
    }
  }
}
