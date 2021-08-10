package org.simple.clinic.drugs.selection.custom

interface CustomDrugEntryUi {
  fun setDrugDosageText(dosage: String)
  fun moveDrugDosageCursorToBeginning()
  fun showRemoveButton()
  fun hideRemoveButton()
  fun setButtonTextAsSave()
  fun setButtonTextAsAdd()
}
