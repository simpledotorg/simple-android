package org.simple.clinic.overdue.communication

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.simple.clinic.sync.DataPullResponse

@JsonClass(generateAdapter = true)
data class CommunicationPullResponse(

    @Json(name = "communications")
    override val payloads: List<CommunicationPayload>,

    @Json(name = "process_token")
    override val processToken: String

) : DataPullResponse<CommunicationPayload>
