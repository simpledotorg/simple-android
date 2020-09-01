package org.simple.clinic.teleconsultlog.teleconsultrecord

import java.util.UUID

data class TeleconsultRequestInfo(
    val requesterId: UUID,

    val facilityId: UUID,

    val requestedAt: String
)
