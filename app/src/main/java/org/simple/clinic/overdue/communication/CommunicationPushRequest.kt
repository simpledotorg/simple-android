package org.simple.clinic.overdue.communication

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CommunicationPushRequest(

    @Json(name = "communications")
    val communications: List<CommunicationPayload>
)
