package org.simple.clinic.drugs.selection.custom

interface CustomDrugEntryUi {
  fun setDrugDosageText(dosage: String)
  fun moveDrugDosageCursorToBeginning()
  fun hideRemoveButton()
  fun setButtonTextAsAdd()
}
