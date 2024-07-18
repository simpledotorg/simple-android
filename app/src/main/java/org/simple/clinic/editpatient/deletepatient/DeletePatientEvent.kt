package org.simple.clinic.editpatient.deletepatient

import org.simple.clinic.patient.DeletedReason
import org.simple.clinic.patient.Patient
import org.simple.clinic.widgets.UiEvent

sealed class DeletePatientEvent : UiEvent

data object PatientDeleted : DeletePatientEvent()

data object PatientMarkedAsDead : DeletePatientEvent()

data class PatientDeleteReasonClicked(val patientDeleteReason: PatientDeleteReason) : DeletePatientEvent()

data class PatientLoaded(val patient: Patient) : DeletePatientEvent()

data class ConfirmPatientDeleteClicked(val deletedReason: DeletedReason) : DeletePatientEvent()

data object ConfirmPatientDiedClicked : DeletePatientEvent()
