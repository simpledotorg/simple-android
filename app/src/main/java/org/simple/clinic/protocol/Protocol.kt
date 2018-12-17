package org.simple.clinic.protocol

import java.util.UUID

@Deprecated(message = "Use v2")
data class Protocol(
    val uuid: UUID,
    val name: String,
    val drugs: List<ProtocolDrug>
)
