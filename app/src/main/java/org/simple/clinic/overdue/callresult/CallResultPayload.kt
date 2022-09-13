package org.simple.clinic.overdue.callresult

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.simple.clinic.overdue.AppointmentCancelReason
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.storage.Timestamps
import java.time.Instant
import java.util.UUID

@JsonClass(generateAdapter = true)
data class CallResultPayload(

    @Json(name = "id")
    val id: UUID,

    @Json(name = "user_id")
    val userId: UUID,

    @Json(name = "patient_id")
    val patientId: UUID?,

    @Json(name = "facility_id")
    val facilityId: UUID?,

    @Json(name = "appointment_id")
    val appointmentId: UUID,

    @Json(name = "remove_reason")
    val removeReason: AppointmentCancelReason?,

    @Json(name = "result_type")
    val outcome: Outcome,

    @Json(name = "created_at")
    val createdAt: Instant,

    @Json(name = "updated_at")
    val updatedAt: Instant,

    @Json(name = "deleted_at")
    val deletedAt: Instant?
) {

  fun toDatabaseModel(syncStatus: SyncStatus): CallResult {
    return CallResult(
        id = id,
        userId = userId,
        patientId = patientId,
        facilityId = facilityId,
        appointmentId = appointmentId,
        removeReason = removeReason,
        outcome = outcome,
        timestamps = Timestamps(
            createdAt = createdAt,
            updatedAt = updatedAt,
            deletedAt = deletedAt
        ),
        syncStatus = syncStatus
    )
  }
}
