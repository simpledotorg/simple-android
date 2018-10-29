package org.simple.clinic.protocol

/**
 * Drugs recommended in a [Protocol].
 */
data class ProtocolDrug(
    val name: String,
    val rxNormCode: String?,
    val dosages: List<String>
)
