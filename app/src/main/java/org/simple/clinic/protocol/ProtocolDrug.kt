package org.simple.clinic.protocol

import java.util.UUID

/**
 * Drugs recommended in a [Protocol].
 */
data class ProtocolDrug(
    val uuid: UUID,
    val name: String,
    val rxNormCode: String?,
    val dosages: List<String>,
    val protocolUUID: UUID
)
