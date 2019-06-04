package org.simple.clinic.summary

data class PatientSummaryItems(
    val prescriptionItems: SummaryPrescribedDrugsItem,
    val bloodPressureListItems: List<SummaryBloodPressureListItem>,
    val medicalHistoryItems: SummaryMedicalHistoryItem
)
