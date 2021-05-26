package org.simple.clinic.summary.linkId

import org.simple.clinic.patient.businessid.Identifier
import java.util.UUID

sealed class LinkIdWithPatientEffect

object CloseSheetWithOutIdLinked : LinkIdWithPatientEffect()

object CloseSheetWithLinkedId : LinkIdWithPatientEffect()

data class AddIdentifierToPatient(
    val patientUuid: UUID,
    val identifier: Identifier
) : LinkIdWithPatientEffect()

data class GetPatientNameFromId(val patientUuid: UUID) : LinkIdWithPatientEffect()
