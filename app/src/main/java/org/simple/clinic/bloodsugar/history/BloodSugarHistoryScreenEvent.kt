package org.simple.clinic.bloodsugar.history

import org.simple.clinic.bloodsugar.BloodSugarMeasurement
import org.simple.clinic.patient.Patient
import org.simple.clinic.widgets.UiEvent

sealed class BloodSugarHistoryScreenEvent : UiEvent

data class PatientLoaded(val patient: Patient) : BloodSugarHistoryScreenEvent()

object AddNewBloodSugarClicked : BloodSugarHistoryScreenEvent() {
  override val analyticsName: String = "Blood Sugar History:Add New Blood Sugar"
}

data class BloodSugarClicked(val bloodSugarMeasurement: BloodSugarMeasurement) : BloodSugarHistoryScreenEvent() {
  override val analyticsName: String = "Blood Sugar History:Blood Sugar Clicked"
}
