package org.simple.clinic.bloodsugar.sync

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.simple.clinic.bloodsugar.BloodSugarMeasurement
import org.simple.clinic.bloodsugar.BloodSugarMeasurementType
import org.simple.clinic.bloodsugar.BloodSugarReading
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.storage.Timestamps
import java.time.Instant
import java.util.UUID

@JsonClass(generateAdapter = true)
data class BloodSugarMeasurementPayload(
    @Json(name = "id")
    val uuid: UUID,

    @Json(name = "blood_sugar_type")
    val bloodSugarType: BloodSugarMeasurementType,

    @Json(name = "blood_sugar_value")
    val bloodSugarValue: Float,

    @Json(name = "patient_id")
    val patientUuid: UUID,

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
    val recordedAt: Instant
) {

  fun toDatabaseModel(syncStatus: SyncStatus) = BloodSugarMeasurement(
      uuid = uuid,
      reading = BloodSugarReading(
          value = bloodSugarValue.toString(),
          type = bloodSugarType
      ),
      syncStatus = syncStatus,
      userUuid = userUuid,
      facilityUuid = facilityUuid,
      patientUuid = patientUuid,
      recordedAt = recordedAt,
      timestamps = Timestamps(
          createdAt = createdAt,
          updatedAt = updatedAt,
          deletedAt = deletedAt
      )
  )
}
