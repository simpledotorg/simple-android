package org.simple.clinic.protocol

/**
 * Drugs recommended in a [Protocol].
 */
@Deprecated(message = "Use v2")
data class ProtocolDrug(
    val name: String,
    val rxNormCode: String?,
    val dosages: List<String>
)
