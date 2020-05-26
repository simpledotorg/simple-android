package org.simple.clinic.editpatient.deletepatient

import org.simple.clinic.patient.Patient

sealed class DeletePatientEvent

object PatientDeleted : DeletePatientEvent()

object PatientMarkedAsDead : DeletePatientEvent()

data class PatientDeleteReasonClicked(val patientDeleteReason: PatientDeleteReason) : DeletePatientEvent()

data class PatientLoaded(val patient: Patient) : DeletePatientEvent()
