package org.simple.clinic.protocol

import java.util.UUID

data class Protocol(
    val uuid: UUID,
    val name: String,
    val drugs: List<ProtocolDrug>
)
