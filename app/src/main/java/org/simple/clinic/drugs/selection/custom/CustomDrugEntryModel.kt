package org.simple.clinic.drugs.selection.custom

data class CustomDrugEntryModel(
    val dosage: String?
) {
  companion object {
    fun default() = CustomDrugEntryModel(dosage = null)
  }

  fun dosageEdited(dosage: String?): CustomDrugEntryModel {
    return copy(dosage = dosage)
  }
}
