package org.simple.clinic.questionnaireresponse.sync

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class QuestionnaireResponsePushRequest(

    @Json(name = "questionnaire_responses")
    val questionnaireResponses: List<QuestionnaireResponsePayload>
)
