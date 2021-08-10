package org.simple.clinic.drugs.selection.custom

data class CustomDrugEntryModel(
    val dosage: String?,
    val dosageHasFocus: Boolean?
) {
  companion object {
    fun default() = CustomDrugEntryModel(dosage = null, dosageHasFocus = null)
  }

  fun dosageEdited(dosage: String?): CustomDrugEntryModel {
    return copy(dosage = dosage)
  }

  fun dosageFocusChanged(hasFocus: Boolean): CustomDrugEntryModel {
    return copy(dosageHasFocus = hasFocus)
  }
}
