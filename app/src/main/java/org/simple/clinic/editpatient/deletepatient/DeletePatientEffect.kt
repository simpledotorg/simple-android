package org.simple.clinic.editpatient.deletepatient

import org.simple.clinic.patient.DeletedReason
import java.util.UUID

sealed class DeletePatientEffect

data class ShowConfirmDeleteDialog(
    val patientName: String,
    val deletedReason: DeletedReason
) : DeletePatientEffect()

data class ShowConfirmDiedDialog(val patientName: String) : DeletePatientEffect()

data class DeletePatient(
    val patientUuid: UUID,
    val deletedReason: DeletedReason
) : DeletePatientEffect()

data class MarkPatientAsDead(val patientUuid: UUID) : DeletePatientEffect()

object ShowHomeScreen : DeletePatientEffect()

data class LoadPatient(val patientUuid: UUID) : DeletePatientEffect()
