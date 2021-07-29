package org.simple.clinic.drugs.selection.custom

import org.simple.clinic.drugs.search.DrugFrequency

data class CustomDrugEntryModel(
    val dosage: String?,
    val frequency: DrugFrequency?
) {
  companion object {
    fun default() = CustomDrugEntryModel(dosage = null, frequency = DrugFrequency.Unknown("None"))
  }

  fun dosageEdited(dosage: String?): CustomDrugEntryModel {
    return copy(dosage = dosage)
  }

  fun frequencyEdited(frequency: DrugFrequency): CustomDrugEntryModel {
    return copy(frequency = frequency)
  }
}
