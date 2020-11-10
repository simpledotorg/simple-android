package org.simple.clinic.bloodsugar.entry

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.bloodsugar.BloodSugarMeasurementType
import org.simple.clinic.bloodsugar.BloodSugarReading
import org.simple.clinic.bloodsugar.BloodSugarUnitPreference
import org.simple.clinic.bloodsugar.entry.BloodSugarEntrySheet.ScreenType
import org.simple.clinic.bloodsugar.entry.BloodSugarEntrySheet.ScreenType.BLOOD_SUGAR_ENTRY
import org.simple.clinic.bloodsugar.entry.BloodSugarSaveState.NOT_SAVING_BLOOD_SUGAR
import java.time.LocalDate

@Parcelize
data class BloodSugarEntryModel(
    val year: String,
    val openAs: OpenAs,
    val bloodSugarReading: BloodSugarReading = BloodSugarReading("", openAs.measurementType),
    val activeScreen: ScreenType = BLOOD_SUGAR_ENTRY,
    val day: String = "",
    val month: String = "",
    val fourDigitYear: String = "",
    val prefilledDate: LocalDate? = null,
    val bloodSugarSaveState: BloodSugarSaveState = NOT_SAVING_BLOOD_SUGAR,
    val bloodSugarUnitPreference: BloodSugarUnitPreference = BloodSugarUnitPreference.Mg,
    val bloodSugarReadingValue: String = "",
    val bloodSugarMeasurementType: BloodSugarMeasurementType = openAs.measurementType
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

  fun yearChanged(fourDigitYear: String): BloodSugarEntryModel =
      copy(fourDigitYear = fourDigitYear)

  fun screenChanged(activeScreen: ScreenType): BloodSugarEntryModel =
      copy(activeScreen = activeScreen)

  fun datePrefilled(prefilledDate: LocalDate): BloodSugarEntryModel =
      copy(prefilledDate = prefilledDate)

  fun bloodSugarStateChanged(bloodSugarSaveState: BloodSugarSaveState): BloodSugarEntryModel =
      copy(bloodSugarSaveState = bloodSugarSaveState)

  fun bloodSugarUnitChanged(bloodSugarUnitPreference: BloodSugarUnitPreference): BloodSugarEntryModel {
    return copy(bloodSugarUnitPreference = bloodSugarUnitPreference)
  }
}
