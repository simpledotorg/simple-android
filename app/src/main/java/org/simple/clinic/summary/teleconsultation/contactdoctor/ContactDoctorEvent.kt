package org.simple.clinic.summary.teleconsultation.contactdoctor

import org.simple.clinic.summary.PatientTeleconsultationInfo
import org.simple.clinic.summary.teleconsultation.sync.MedicalOfficer
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

sealed class ContactDoctorEvent : UiEvent

data class MedicalOfficersLoaded(val medicalOfficers: List<MedicalOfficer>) : ContactDoctorEvent()

data class TeleconsultRequestCreated(
    val teleconsultRecordId: UUID,
    val doctorPhoneNumber: String,
    val messageTarget: MessageTarget
) : ContactDoctorEvent()

data class PatientTeleconsultInfoLoaded(
    val patientTeleconsultInfo: PatientTeleconsultationInfo,
    val messageTarget: MessageTarget
) : ContactDoctorEvent()

data class WhatsAppButtonClicked(
    val medicalOfficerId: UUID,
    val doctorPhoneNumber: String
) : ContactDoctorEvent() {

  override val analyticsName: String = "Contact Doctor Sheet:WhatsApp Clicked"
}

data class SmsButtonClicked(
    val medicalOfficerId: UUID,
    val doctorPhoneNumber: String
) : ContactDoctorEvent() {

  override val analyticsName: String = "Contact Doctor Sheet:SMS Clicked"
}
