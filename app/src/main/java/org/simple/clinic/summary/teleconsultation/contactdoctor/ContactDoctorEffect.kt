package org.simple.clinic.summary.teleconsultation.contactdoctor

import java.util.UUID

sealed class ContactDoctorEffect

object LoadMedicalOfficers : ContactDoctorEffect()

data class CreateTeleconsultRequest(val patientUuid: UUID, val medicalOfficerId: UUID, val doctorPhoneNumber: String) : ContactDoctorEffect()

data class LoadPatientTeleconsultInfo(
    val patientUuid: UUID,
    val teleconsultRecordId: UUID,
    val doctorPhoneNumber: String
) : ContactDoctorEffect()
