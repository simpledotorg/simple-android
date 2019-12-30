package org.simple.clinic.bloodsugar.entry

import org.simple.clinic.bloodsugar.entry.BloodSugarEntrySheet.ScreenType
import org.simple.clinic.bloodsugar.entry.BloodSugarEntrySheet.ScreenType.BLOOD_SUGAR_ENTRY
import org.threeten.bp.LocalDate

data class BloodSugarEntryModel(
    val year: String,
    val openAs: OpenAs,
    val bloodSugarReading: String = "",
    val activeScreen: ScreenType = BLOOD_SUGAR_ENTRY,
    val day: String = "",
    val month: String = "",
    val twoDigitYear: String = "",
    val prefilledDate: LocalDate? = null
) {

  companion object {
    fun create(year: Int, openAs: OpenAs) =
        BloodSugarEntryModel(year.toString(), openAs)
  }

  fun bloodSugarChanged(bloodSugarReading: String): BloodSugarEntryModel =
      copy(bloodSugarReading = bloodSugarReading)

  fun dayChanged(day: String): BloodSugarEntryModel =
      copy(day = day)

  fun monthChanged(month: String): BloodSugarEntryModel =
      copy(month = month)

  fun yearChanged(twoDigitYear: String): BloodSugarEntryModel =
      copy(twoDigitYear = twoDigitYear)

  fun screenChanged(activeScreen: ScreenType): BloodSugarEntryModel =
      copy(activeScreen = activeScreen)

  fun datePrefilled(prefilledDate: LocalDate): BloodSugarEntryModel =
      copy(prefilledDate = prefilledDate)
}
