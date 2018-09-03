package org.simple.clinic.overdue.communication

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.threeten.bp.Instant
import java.util.UUID

@JsonClass(generateAdapter = true)
data class CommunicationPayload(

    @Json(name = "id")
    val uuid: UUID,

    @Json(name = "appointment_id")
    val appointmentUuid: UUID,

    @Json(name = "user_id")
    val userUuid: UUID,

    @Json(name = "communication_type")
    val type: Communication.Type,

    @Json(name = "communication_result")
    val result: Communication.Result,

    @Json(name = "created_at")
    val createdAt: Instant,

    @Json(name = "updated_at")
    val updatedAt: Instant
)
