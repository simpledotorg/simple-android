package org.simple.clinic.teleconsultlog.teleconsultrecord

import java.time.Instant
import java.util.UUID

data class TeleconsultRequestInfo(
    val requesterId: UUID,

    val facilityId: UUID,

    val requestedAt: Instant,

    val requesterCompletionStatus: TeleconsultStatus?
)
