package org.simple.clinic.questionnaireresponse.sync

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.simple.clinic.questionnaire.QuestionnaireType
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.questionnaireresponse.QuestionnaireResponse
import org.simple.clinic.storage.Timestamps
import java.time.Instant
import java.util.UUID

@JsonClass(generateAdapter = true)
data class QuestionnaireResponsePayload(
    @Json(name = "id")
    val uuid: UUID,

    @Json(name = "questionnaire_id")
    val questionnaireId: UUID,

    @Json(name = "questionnaire_type")
    val questionnaireType: QuestionnaireType,

    @Json(name = "facility_id")
    val facilityId: UUID,

    @Json(name = "last_updated_by_user_id")
    val lastUpdatedByUserId: UUID,

    @Json(name = "content")
    val content: Map<String, String>,

    @Json(name = "created_at")
    val createdAt: Instant,

    @Json(name = "updated_at")
    val updatedAt: Instant,

    @Json(name = "deleted_at")
    val deletedAt: Instant?,
) {

  fun toDatabaseModel(syncStatus: SyncStatus) = QuestionnaireResponse(
      uuid = uuid,
      questionnaireId = questionnaireId,
      questionnaireType = questionnaireType,
      facilityId = facilityId,
      lastUpdatedByUserId = lastUpdatedByUserId,
      content = content,
      timestamps = Timestamps(
          createdAt = createdAt,
          updatedAt = updatedAt,
          deletedAt = deletedAt
      ),
      syncStatus = syncStatus,
  )
}
