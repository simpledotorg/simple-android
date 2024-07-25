package org.simple.clinic.bloodsugar.history

import androidx.paging.PagingData
import org.simple.clinic.bloodsugar.BloodSugarMeasurement
import org.simple.clinic.bloodsugar.history.adapter.BloodSugarHistoryListItem
import org.simple.clinic.patient.Patient
import org.simple.clinic.widgets.UiEvent

sealed class BloodSugarHistoryScreenEvent : UiEvent

data class PatientLoaded(val patient: Patient) : BloodSugarHistoryScreenEvent()

data object AddNewBloodSugarClicked : BloodSugarHistoryScreenEvent() {
  override val analyticsName: String = "Blood Sugar History:Add New Blood Sugar"
}

data class BloodSugarClicked(val bloodSugarMeasurement: BloodSugarMeasurement) : BloodSugarHistoryScreenEvent() {
  override val analyticsName: String = "Blood Sugar History:Blood Sugar Clicked"
}

data class BloodSugarHistoryLoaded(
    val bloodSugars: PagingData<BloodSugarHistoryListItem>
) : BloodSugarHistoryScreenEvent()
