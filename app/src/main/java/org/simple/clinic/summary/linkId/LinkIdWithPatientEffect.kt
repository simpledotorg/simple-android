package org.simple.clinic.summary.linkId

import org.simple.clinic.patient.businessid.Identifier
import java.util.UUID

sealed class LinkIdWithPatientEffect

data class AddIdentifierToPatient(
    val patientUuid: UUID,
    val identifier: Identifier
) : LinkIdWithPatientEffect()

data class GetPatientNameFromId(val patientUuid: UUID) : LinkIdWithPatientEffect()

sealed class LinkIdWithPatientViewEffect : LinkIdWithPatientEffect()

data object CloseSheetWithOutIdLinked : LinkIdWithPatientViewEffect()

data object CloseSheetWithLinkedId : LinkIdWithPatientViewEffect()
