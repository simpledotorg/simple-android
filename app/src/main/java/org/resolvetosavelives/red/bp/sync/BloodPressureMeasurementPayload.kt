package org.resolvetosavelives.red.bp.sync

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.resolvetosavelives.red.bp.BloodPressureMeasurement
import org.resolvetosavelives.red.patient.SyncStatus
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

    @Json(name = "created_at")
    val createdAt: Instant,

    @Json(name = "updated_at")
    val updatedAt: Instant
) {

  fun toDatabaseModel(syncStatus: SyncStatus): BloodPressureMeasurement {
    return BloodPressureMeasurement(
        uuid = uuid,
        systolic = systolic,
        diastolic = diastolic,
        createdAt = createdAt,
        updatedAt = updatedAt,
        syncStatus = syncStatus,
        patientUuid = patientUuid
    )
  }
}
