package org.simple.clinic.editpatient.deletepatient

sealed class PatientDeleteReason {
  object Duplicate : PatientDeleteReason()

  object AccidentalRegistration : PatientDeleteReason()

  object Died : PatientDeleteReason()
}
