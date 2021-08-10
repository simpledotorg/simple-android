package org.simple.clinic.drugs.selection.custom

import org.simple.clinic.drugs.search.DrugFrequency

data class CustomDrugEntryModel(
    val dosage: String?,
    val dosageHasFocus: Boolean?,
    val frequency: DrugFrequency?
) {
  companion object {
    fun default() = CustomDrugEntryModel(dosage = null, dosageHasFocus = null, frequency = null)
  }

  fun dosageEdited(dosage: String?): CustomDrugEntryModel {
    return copy(dosage = dosage)
  }

  fun dosageFocusChanged(hasFocus: Boolean): CustomDrugEntryModel {
    return copy(dosageHasFocus = hasFocus)
  }

  fun frequencyEdited(frequency: DrugFrequency?): CustomDrugEntryModel {
    return copy(frequency = frequency)
  }
}
