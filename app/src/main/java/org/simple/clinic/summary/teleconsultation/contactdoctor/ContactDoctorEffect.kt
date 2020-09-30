package org.simple.clinic.summary.teleconsultation.contactdoctor

import org.simple.clinic.summary.PatientTeleconsultationInfo
import java.util.UUID

sealed class ContactDoctorEffect

object LoadMedicalOfficers : ContactDoctorEffect()

data class CreateTeleconsultRequest(
    val patientUuid: UUID,
    val medicalOfficerId: UUID,
    val doctorPhoneNumber: String,
    val messageTarget: MessageTarget
) : ContactDoctorEffect()

data class LoadPatientTeleconsultInfo(
    val patientUuid: UUID,
    val teleconsultRecordId: UUID,
    val doctorPhoneNumber: String,
    val messageTarget: MessageTarget
) : ContactDoctorEffect()

data class SendTeleconsultMessage(val teleconsultInfo: PatientTeleconsultationInfo, val messageTarget: MessageTarget) : ContactDoctorEffect()
