package org.simple.clinic.drugs.selection.custom

import org.simple.clinic.drugs.search.DrugFrequency
import org.simple.clinic.drugs.selection.custom.drugfrequency.country.DrugFrequencyChoiceItem

interface CustomDrugEntrySheetUiActions {
  fun showEditFrequencyDialog(
      frequency: DrugFrequency?,
      drugFrequencyChoiceItems: List<DrugFrequencyChoiceItem>
  )

  fun setDrugFrequency(frequencyLabelRes: Int)
  fun setDrugDosage(dosage: String?)
  fun closeSheetAndGoToEditMedicineScreen()
  fun hideKeyboard()
}
