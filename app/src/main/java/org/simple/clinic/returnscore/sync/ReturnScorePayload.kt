package org.simple.clinic.returnscore.sync

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.simple.clinic.returnscore.ReturnScore
import org.simple.clinic.returnscore.ScoreType
import org.simple.clinic.storage.Timestamps
import java.time.Instant
import java.util.UUID

@JsonClass(generateAdapter = true)
data class ReturnScorePayload(
    @Json(name = "id")
    val uuid: UUID,

    @Json(name = "patient_id")
    val patientUuid: UUID,

    @Json(name = "score_type")
    val scoreType: ScoreType,

    @Json(name = "score_value")
    val scoreValue: Float,

    @Json(name = "created_at")
    val createdAt: Instant,

    @Json(name = "updated_at")
    val updatedAt: Instant,

    @Json(name = "deleted_at")
    val deletedAt: Instant?,
) {

  fun toDatabaseModel() = ReturnScore(
      uuid = uuid,
      patientUuid = patientUuid,
      scoreType = scoreType,
      scoreValue = scoreValue,
      timestamps = Timestamps(
          createdAt = createdAt,
          updatedAt = updatedAt,
          deletedAt = deletedAt
      )
  )
}
