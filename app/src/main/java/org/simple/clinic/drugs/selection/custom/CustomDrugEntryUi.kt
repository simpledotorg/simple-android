package org.simple.clinic.drugs.selection.custom

interface CustomDrugEntryUi {
  fun setDrugDosageText(dosage: String)
  fun moveDrugDosageCursorToBeginning()
  fun showRemoveButton()
  fun hideRemoveButton()
  fun setButtonTextAsSave()
  fun setButtonTextAsAdd()
  fun setSheetTitle(drugName: String?, dosage: String?, frequencyLabel: String)
  fun showProgressBar()
  fun hideCustomDrugEntryUi()
  fun hideProgressBar()
  fun showCustomDrugEntryUi()
  fun showSaveButtonProgressState()
}
