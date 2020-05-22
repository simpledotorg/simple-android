package org.simple.clinic.editpatient.deletepatient

import org.simple.clinic.patient.DeletedReason

sealed class DeletePatientEffect

data class ShowConfirmDeleteDialog(val patientName: String, val deletedReason: DeletedReason) : DeletePatientEffect()

data class ShowConfirmDiedDialog(val patientName: String) : DeletePatientEffect()
