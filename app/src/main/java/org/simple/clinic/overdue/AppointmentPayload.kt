package org.simple.clinic.overdue

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import java.util.UUID

@JsonClass(generateAdapter = true)
data class AppointmentPayload(

    @Json(name = "id")
    val uuid: UUID,

    @Json(name = "patient_id")
    val patientUuid: UUID,

    @Json(name = "facility_id")
    val facilityUuid: UUID,

    @Json(name = "scheduled_date")
    val date: LocalDate,

    @Json(name = "status")
    val status: Appointment.Status,

    @Json(name = "cancel_reason")
    val cancelReason: AppointmentCancelReason?,

    @Json(name = "remind_on")
    val remindOn: LocalDate?,

    @Json(name = "agreed_to_visit")
    val agreedToVisit: Boolean?,

    @Json(name = "appointment_type")
    val appointmentType: Appointment.AppointmentType?,

    @Json(name = "created_at")
    val createdAt: Instant,

    @Json(name = "updated_at")
    val updatedAt: Instant,

    @Json(name = "deleted_at")
    val deletedAt: Instant?
)
