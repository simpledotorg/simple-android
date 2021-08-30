package org.simple.clinic.drugs.selection.custom

import org.simple.clinic.drugs.search.DrugFrequency

interface CustomDrugEntrySheetUiActions {
  fun showEditFrequencyDialog(frequency: DrugFrequency?)
  fun setDrugFrequency(frequencyLabelRes: Int)
  fun setDrugDosage(dosage: String?)
  fun closeSheetAndGoToEditMedicineScreen()
}
