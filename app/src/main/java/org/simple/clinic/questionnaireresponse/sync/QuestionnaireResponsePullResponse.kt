package org.simple.clinic.questionnaireresponse.sync

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.simple.clinic.sync.DataPullResponse

@JsonClass(generateAdapter = true)
data class QuestionnaireResponsePullResponse(

    @Json(name = "questionnaire_responses")
    override val payloads: List<QuestionnaireResponsePayload>,

    @Json(name = "process_token")
    override val processToken: String

) : DataPullResponse<QuestionnaireResponsePayload>
