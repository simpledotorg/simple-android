package org.simple.clinic.summary.teleconsultation.contactdoctor

import org.simple.clinic.summary.teleconsultation.sync.MedicalOfficer

sealed class ContactDoctorEvent

data class MedicalOfficersLoaded(val medicalOfficers: List<MedicalOfficer>) : ContactDoctorEvent()
