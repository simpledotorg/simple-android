package org.simple.clinic.questionnaire.sync

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.simple.clinic.sync.DataPullResponse

@JsonClass(generateAdapter = true)
data class QuestionnairePullResponse(

    @Json(name = "questionnaires")
    override val payloads: List<QuestionnairePayload>,

    @Json(name = "process_token")
    override val processToken: String

) : DataPullResponse<QuestionnairePayload>
