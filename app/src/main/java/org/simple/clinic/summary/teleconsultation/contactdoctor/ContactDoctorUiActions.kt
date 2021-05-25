package org.simple.clinic.summary.teleconsultation.contactdoctor

import org.simple.clinic.summary.PatientTeleconsultationInfo

interface ContactDoctorUiActions {
  fun sendTeleconsultMessage(
      teleconsultInfo: PatientTeleconsultationInfo,
      messageTarget: MessageTarget
  )
}
