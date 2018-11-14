package org.simple.clinic.summary

import org.simple.clinic.bp.BloodPressureMeasurement

data class PatientSummaryItems(
    val prescriptionItems: SummaryPrescribedDrugsItem,
    val bloodPressures: List<BloodPressureMeasurement>,
    val bloodPressureListItems: List<SummaryBloodPressureListItem>,
    val medicalHistoryItems: SummaryMedicalHistoryItem
) {
}
