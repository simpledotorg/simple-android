package org.simple.clinic.drugs.selection.custom

import org.simple.clinic.drugs.search.DrugFrequency

interface CustomDrugEntrySheetUiActions {
  fun showEditFrequencyDialog(frequency: DrugFrequency?)
  fun setDrugFrequency(frequencyLabel: String)
  fun setDrugDosage(dosage: String?)
  fun closeSheetAndGoToEditMedicineScreen()
  fun hideKeyboard()
  fun showKeyboard()
  fun clearFocusFromDosageEditText()
  fun setCursorPosition(position: Int)
}
