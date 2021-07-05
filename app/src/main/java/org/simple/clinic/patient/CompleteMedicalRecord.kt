package org.simple.clinic.patient

import org.simple.clinic.bloodsugar.BloodSugarMeasurement
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.medicalhistory.MedicalHistory
import org.simple.clinic.overdue.Appointment

data class CompleteMedicalRecord(
    val patient: PatientProfile,
    val medicalHistory: MedicalHistory?,
    val appointments: List<Appointment>,
    val bloodPressures: List<BloodPressureMeasurement>,
    val bloodSugars: List<BloodSugarMeasurement>,
    val prescribedDrugs: List<PrescribedDrug>
)
