package org.simple.clinic.bp.sync

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.util.createUuid5
import org.threeten.bp.Instant
import java.util.UUID

@JsonClass(generateAdapter = true)
data class BloodPressureMeasurementPayload(
    @Json(name = "id")
    val uuid: UUID,

    @Json(name = "patient_id")
    val patientUuid: UUID,

    @Json(name = "systolic")
    val systolic: Int,

    @Json(name = "diastolic")
    val diastolic: Int,

    @Json(name = "facility_id")
    val facilityUuid: UUID,

    @Json(name = "user_id")
    val userUuid: UUID,

    @Json(name = "created_at")
    val createdAt: Instant,

    @Json(name = "updated_at")
    val updatedAt: Instant,

    @Json(name = "deleted_at")
    val deletedAt: Instant?,

    @Json(name = "recorded_at")
    val recordedAt: Instant?
) {

  fun toDatabaseModel(syncStatus: SyncStatus): BloodPressureMeasurement {
    return BloodPressureMeasurement(
        uuid = uuid,
        systolic = systolic,
        diastolic = diastolic,
        syncStatus = syncStatus,
        userUuid = userUuid,
        facilityUuid = facilityUuid,
        patientUuid = patientUuid,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt,

        // recordedAt should be changed here when BloodPressureMeasurementPayload starts receiving this field from server
        recordedAt = recordedAt ?: createdAt,
        encounterUuid = createUuid5(facilityUuid.toString() + patientUuid + recordedAt))
  }
}
