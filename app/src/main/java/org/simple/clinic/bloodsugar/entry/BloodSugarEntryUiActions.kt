package org.simple.clinic.bloodsugar.entry

interface BloodSugarEntryUiActions {
  fun setBloodSugarReading(bloodSugarReading: String)
  fun hideBloodSugarErrorMessage()
  fun hideDateErrorMessage()
}
