package org.simple.clinic.editpatient.deletepatient

sealed class DeletePatientEvent

object PatientDeleted : DeletePatientEvent()

object PatientMarkedAsDead : DeletePatientEvent()

data class PatientDeleteReasonClicked(val patientDeleteReason: PatientDeleteReason) : DeletePatientEvent()
