package org.simple.clinic.bloodsugar.entry

import org.simple.clinic.bloodsugar.BloodSugarMeasurementType
import org.simple.clinic.bloodsugar.BloodSugarUnitPreference
import java.time.LocalDate
import java.util.UUID

interface BloodSugarEntryUiActions {
  fun setBloodSugarReading(bloodSugarReading: String)
  fun hideBloodSugarErrorMessage()
  fun hideDateErrorMessage()
  fun dismiss()
  fun showDateEntryScreen()
  fun setBloodSugarSavedResultAndFinish()
  fun showConfirmRemoveBloodSugarDialog(bloodSugarMeasurementUuid: UUID)
  fun showBloodSugarUnitSelectionDialog(bloodSugarUnitPreference: BloodSugarUnitPreference)
  fun showBloodSugarEmptyError()
  fun showBloodSugarHighError(
      measurementType: BloodSugarMeasurementType,
      unitPreference: BloodSugarUnitPreference
  )
  fun showBloodSugarLowError(
      measurementType: BloodSugarMeasurementType,
      unitPreference: BloodSugarUnitPreference
  )
  fun showInvalidDateError()
  fun showDateIsInFutureError()
  fun showBloodSugarEntryScreen()
  fun showBloodSugarDate(date: LocalDate)
  fun setDateOnInputFields(date: LocalDate)
}
