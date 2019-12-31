package org.simple.clinic.bloodsugar.entry

data class BloodSugarEntryModel(
    val bloodSugarReading: String = ""
) {

  companion object {
    val BLANK = BloodSugarEntryModel()
  }

  fun bloodSugarChanged(bloodSugarReading: String): BloodSugarEntryModel =
      copy(bloodSugarReading = bloodSugarReading)

  fun dayChanged(): BloodSugarEntryModel =
      copy()

  fun monthChanged(): BloodSugarEntryModel =
      copy()

  fun yearChanged(): BloodSugarEntryModel =
      copy()
}
