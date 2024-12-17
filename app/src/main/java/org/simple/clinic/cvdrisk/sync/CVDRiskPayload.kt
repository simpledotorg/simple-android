package org.simple.clinic.cvdrisk.sync

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.simple.clinic.cvdrisk.CVDRisk
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.storage.Timestamps
import java.time.Instant
import java.util.UUID

@JsonClass(generateAdapter = true)
data class CVDRiskPayload(

    @Json(name = "id")
    val uuid: UUID,

    @Json(name = "patient_id")
    val patientUuid: UUID,

    //TODO - Update the type to string when server fix the type
    @Json(name = "risk_score")
    val riskScore: Int,

    @Json(name = "created_at")
    val createdAt: Instant,

    @Json(name = "updated_at")
    val updatedAt: Instant,

    @Json(name = "deleted_at")
    val deletedAt: Instant?,
) {

  fun toDatabaseModel(syncStatus: SyncStatus) = CVDRisk(
      uuid = uuid,
      patientUuid = patientUuid,
      riskScore = riskScore.toString(),
      timestamps = Timestamps(
          createdAt = createdAt,
          updatedAt = updatedAt,
          deletedAt = deletedAt
      ),
      syncStatus = syncStatus,
  )
}
