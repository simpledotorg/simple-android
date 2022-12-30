package org.simple.clinic.monthlyReports.questionnaire.sync

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.json.JSONObject
import org.simple.clinic.monthlyReports.questionnaire.Questionnaire
import org.simple.clinic.monthlyReports.questionnaire.QuestionnaireLayout
import org.simple.clinic.monthlyReports.questionnaire.QuestionnaireType
import java.time.Instant
import java.util.UUID

@JsonClass(generateAdapter = true)
data class QuestionnairePayload(
    @Json(name = "id")
    val uuid: UUID,

    @Json(name = "questionnaire_type")
    val questionnaireType: QuestionnaireType,

    @Json(name = "layout")
    val layout: QuestionnaireLayout,

    @Json(name = "deleted_at")
    val deletedAt: Instant?,
) {

  fun toDatabaseModel() = Questionnaire(
      uuid = uuid,
      questionnaire_type = questionnaireType,
      layout = layout,
      deletedAt = deletedAt
  )
}
