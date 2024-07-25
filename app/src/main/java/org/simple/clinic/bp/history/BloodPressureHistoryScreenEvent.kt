package org.simple.clinic.bp.history

import androidx.paging.PagingData
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.bp.history.adapter.BloodPressureHistoryListItem
import org.simple.clinic.patient.Patient
import org.simple.clinic.widgets.UiEvent

sealed class BloodPressureHistoryScreenEvent : UiEvent

data class PatientLoaded(val patient: Patient) : BloodPressureHistoryScreenEvent()

data object NewBloodPressureClicked : BloodPressureHistoryScreenEvent()

data class BloodPressureClicked(val bloodPressureMeasurement: BloodPressureMeasurement) : BloodPressureHistoryScreenEvent()

data class BloodPressuresHistoryLoaded(
    val bloodPressures: PagingData<BloodPressureHistoryListItem>
) : BloodPressureHistoryScreenEvent()
