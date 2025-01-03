package org.simple.clinic.patientattribute.sync

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.patientattribute.BMIReading
import org.simple.clinic.patientattribute.PatientAttribute
import org.simple.clinic.storage.Timestamps
import java.time.Instant
import java.util.UUID

@JsonClass(generateAdapter = true)
data class PatientAttributePayload(

    @Json(name = "id")
    val uuid: UUID,

    @Json(name = "height")
    val height: String,

    @Json(name = "weight")
    val weight: String,

    @Json(name = "patient_id")
    val patientUuid: UUID,

    @Json(name = "user_id")
    val userUuid: UUID,

    @Json(name = "created_at")
    val createdAt: Instant,

    @Json(name = "updated_at")
    val updatedAt: Instant,

    @Json(name = "deleted_at")
    val deletedAt: Instant?,
) {

  fun toDatabaseModel(syncStatus: SyncStatus) = PatientAttribute(
      uuid = uuid,
      patientUuid = patientUuid,
      userUuid = userUuid,
      bmiReading = BMIReading(
          height = height.toFloat(),
          weight = weight.toFloat()
      ),
      timestamps = Timestamps(
          createdAt = createdAt,
          updatedAt = updatedAt,
          deletedAt = deletedAt
      ),
      syncStatus = syncStatus,
  )
}
