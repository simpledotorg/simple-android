package org.simple.clinic.summary

import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.medicalhistory.MedicalHistory

data class PatientSummaryItems(
    val prescription: List<PrescribedDrug>,
    val bloodPressures: List<BloodPressureMeasurement>,
    val medicalHistory: MedicalHistory
)
