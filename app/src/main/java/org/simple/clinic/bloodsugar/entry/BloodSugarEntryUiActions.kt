package org.simple.clinic.bloodsugar.entry

import java.util.UUID

interface BloodSugarEntryUiActions {
  fun setBloodSugarReading(bloodSugarReading: String)
  fun hideBloodSugarErrorMessage()
  fun hideDateErrorMessage()
  fun dismiss()
  fun showDateEntryScreen()
  fun setBloodSugarSavedResultAndFinish()
  fun showConfirmRemoveBloodSugarDialog(bloodSugarMeasurementUuid: UUID)
}
