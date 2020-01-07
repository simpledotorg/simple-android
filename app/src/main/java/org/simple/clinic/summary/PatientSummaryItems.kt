package org.simple.clinic.summary

import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.medicalhistory.MedicalHistory

data class PatientSummaryItems(
    val prescription: List<PrescribedDrug>,
    val medicalHistory: MedicalHistory
)
