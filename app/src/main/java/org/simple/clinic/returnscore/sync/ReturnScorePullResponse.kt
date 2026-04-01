package org.simple.clinic.returnscore.sync

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.simple.clinic.sync.DataPullResponse

@JsonClass(generateAdapter = true)
data class ReturnScorePullResponse(

    @Json(name = "patient_scores")
    override val payloads: List<ReturnScorePayload>,

    @Json(name = "process_token")
    override val processToken: String

) : DataPullResponse<ReturnScorePayload>
