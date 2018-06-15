package org.simple.clinic.drugs

import org.threeten.bp.Instant
import java.util.UUID

/**
 * Drugs prescribed to a patient. This may have been picked
 * from a [ProtocolDrug] or entered manually.
 */
data class PrescribedDrug(
    val uuid: UUID,
    val name: String,
    val dosage: String,
    val rxNormCode: String?,
    val patientId: UUID,
    val facilityId: UUID,
    val createdAt: Instant,
    val updatedAt: Instant
)
