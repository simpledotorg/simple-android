package org.simple.clinic.teleconsultlog.teleconsultrecord

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.time.Instant
import java.util.UUID

@JsonClass(generateAdapter = true)
data class TeleconsultRequestInfoPayload(
    @Json(name = "requester_id")
    val requesterId: UUID,

    @Json(name = "facility_id")
    val facilityId: UUID,

    @Json(name = "requested_at")
    val requestedAt: Instant,

    @Json(name = "request_completed")
    val requestCompleted: TeleconsultStatus?
)
