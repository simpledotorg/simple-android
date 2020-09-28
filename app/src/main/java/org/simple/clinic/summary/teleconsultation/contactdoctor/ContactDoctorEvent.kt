package org.simple.clinic.summary.teleconsultation.contactdoctor

import org.simple.clinic.summary.teleconsultation.sync.MedicalOfficer
import java.util.UUID

sealed class ContactDoctorEvent

data class MedicalOfficersLoaded(val medicalOfficers: List<MedicalOfficer>) : ContactDoctorEvent()

data class TeleconsultRequestCreated(val teleconsultRecordId: UUID, val doctorPhoneNumber: String) : ContactDoctorEvent()
