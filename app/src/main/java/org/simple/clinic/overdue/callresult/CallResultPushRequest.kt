package org.simple.clinic.overdue.callresult

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CallResultPushRequest(

    @Json(name = "call_results")
    val callResults: List<CallResultPayload>
)
