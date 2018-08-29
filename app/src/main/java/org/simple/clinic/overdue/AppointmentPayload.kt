package org.simple.clinic.overdue

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.simple.clinic.sync.SynceablePayload
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import java.util.UUID

@JsonClass(generateAdapter = true)
data class AppointmentPayload(
    @Json(name = "id")
    val id: UUID,

    @Json(name = "patient_id")
    val patientId: UUID,

    @Json(name = "facility_id")
    val facilityId: UUID,

    @Json(name = "date")
    val date: LocalDate,

    @Json(name = "status")
    val status: Appointment.Status,

    @Json(name = "status_reason")
    val statusReason: Appointment.StatusReason,

    @Json(name = "created_at")
    val createdAt: Instant,

    @Json(name = "updated_at")
    val updatedAt: Instant

) : SynceablePayload<Appointment>
