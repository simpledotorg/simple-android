package org.simple.clinic.bloodsugar.entry

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.bloodsugar.BloodSugarReading
import org.simple.clinic.bloodsugar.entry.BloodSugarEntrySheet.ScreenType
import org.simple.clinic.bloodsugar.entry.BloodSugarEntrySheet.ScreenType.BLOOD_SUGAR_ENTRY
import org.threeten.bp.LocalDate

@Parcelize
data class BloodSugarEntryModel(
    val year: String,
    val openAs: OpenAs,
    val bloodSugarReading: BloodSugarReading = BloodSugarReading("", openAs.measurementType),
    val activeScreen: ScreenType = BLOOD_SUGAR_ENTRY,
    val day: String = "",
    val month: String = "",
    val twoDigitYear: String = "",
    val prefilledDate: LocalDate? = null
) : Parcelable {

  companion object {
    fun create(year: Int, openAs: OpenAs) =
        BloodSugarEntryModel(year.toString(), openAs)
  }

  fun bloodSugarChanged(bloodSugarReading: String): BloodSugarEntryModel =
      copy(bloodSugarReading = this.bloodSugarReading.readingChanged(bloodSugarReading))

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
