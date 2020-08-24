package org.simple.clinic.summary.teleconsultation.messagebuilder

import org.simple.clinic.summary.PatientTeleconsultationInfo

interface TeleconsultationMessageBuilder {
  fun message(patientTeleconsultationInfo: PatientTeleconsultationInfo): String
}
