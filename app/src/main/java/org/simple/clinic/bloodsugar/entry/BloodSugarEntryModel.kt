package org.simple.clinic.bloodsugar.entry

data class BloodSugarEntryModel(
    val year: String,
    val bloodSugarReading: String = "",
    val day: String = "",
    val month: String = "",
    val twoDigitYear: String = ""
) {

  companion object {
    fun create(year: Int) = BloodSugarEntryModel(year.toString())
  }

  fun bloodSugarChanged(bloodSugarReading: String): BloodSugarEntryModel =
      copy(bloodSugarReading = bloodSugarReading)

  fun dayChanged(day: String): BloodSugarEntryModel =
      copy(day = day)

  fun monthChanged(month: String): BloodSugarEntryModel =
      copy(month = month)

  fun yearChanged(twoDigitYear: String): BloodSugarEntryModel =
      copy(twoDigitYear = twoDigitYear)
}
