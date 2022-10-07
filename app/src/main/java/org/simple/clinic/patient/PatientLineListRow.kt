package org.simple.clinic.patient

import androidx.room.Embedded
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.medicalhistory.Answer
import org.simple.clinic.patient.businessid.BusinessId
import java.time.Instant
import java.util.UUID

data class PatientLineListRow(
    val patientName: String,
    val gender: Gender,
    val status: PatientStatus,
    @Embedded
    val age: PatientAgeDetails,
    val registrationDate: Instant,
    val registrationFacilityId: UUID?,
    val registrationFacilityName: String?,
    val assignedFacilityId: UUID?,
    val assignedFacilityName: String?,
    val streetAddress: String?,
    val colonyOrVillage: String?,
    val patientPhoneNumber: String?,
    val diagnosedWithHypertension: Answer?,
    val diagnosedWithDiabetes: Answer?,
    @Embedded(prefix = "bp_")
    val latestBloodPressureMeasurement: BloodPressureMeasurement?,
    @Embedded(prefix = "bp_passport_")
    val bpPassport: BusinessId?
)
