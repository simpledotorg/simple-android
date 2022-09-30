package org.simple.clinic.patient

import androidx.room.Embedded
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.medicalhistory.Answer
import java.time.Instant

data class PatientLineListRow(
    val patientName: String,
    val gender: Gender,
    val status: PatientStatus,
    @Embedded
    val age: PatientAgeDetails,
    val registrationDate: Instant,
    val registrationFacilityName: String?,
    val assignedFacilityName: String?,
    val streetAddress: String?,
    val colonyOrVillage: String?,
    val patientPhoneNumber: String?,
    val diagnosedWithHypertension: Answer,
    val diagnosedWithDiabetes: Answer,
    @Embedded(prefix = "bp_")
    val latestBloodPressureMeasurement: BloodPressureMeasurement?
)
