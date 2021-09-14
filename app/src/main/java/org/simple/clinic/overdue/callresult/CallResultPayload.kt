package org.simple.clinic.overdue.callresult

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.simple.clinic.overdue.AppointmentCancelReason
import java.time.Instant
import java.util.UUID

@JsonClass(generateAdapter = true)
data class CallResultPayload(

    @Json(name = "id")
    val id: UUID,

    @Json(name = "user_id")
    val userId: UUID,

    @Json(name = "appointment_id")
    val appointmentId: UUID,

    @Json(name = "cancel_reason")
    val removeReason: AppointmentCancelReason?,

    @Json(name = "result")
    val outcome: Outcome,

    @Json(name = "created_at")
    val createdAt: Instant,

    @Json(name = "updated_at")
    val updatedAt: Instant,

    @Json(name = "deleted_at")
    val deletedAt: Instant?
)
