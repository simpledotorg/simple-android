package org.simple.clinic.editpatient.deletepatient

import org.simple.clinic.patient.DeletedReason
import java.util.UUID

sealed class DeletePatientEffect

data class DeletePatient(
    val patientUuid: UUID,
    val deletedReason: DeletedReason
) : DeletePatientEffect()

data class MarkPatientAsDead(val patientUuid: UUID) : DeletePatientEffect()

data class LoadPatient(val patientUuid: UUID) : DeletePatientEffect()

sealed class DeletePatientViewEffect : DeletePatientEffect()

data class ShowConfirmDeleteDialog(
    val patientName: String,
    val deletedReason: DeletedReason
) : DeletePatientViewEffect()

data class ShowConfirmDiedDialog(val patientName: String) : DeletePatientViewEffect()

object ShowHomeScreen : DeletePatientViewEffect()
