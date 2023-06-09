package org.simple.clinic.bloodsugar.entry

interface BloodSugarEntryUiActions {
  fun setBloodSugarReading(bloodSugarReading: String)
  fun hideBloodSugarErrorMessage()
  fun hideDateErrorMessage()
  fun dismiss()
  fun showDateEntryScreen()
  fun setBloodSugarSavedResultAndFinish()
}
