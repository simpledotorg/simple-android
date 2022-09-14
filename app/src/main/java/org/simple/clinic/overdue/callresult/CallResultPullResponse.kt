package org.simple.clinic.overdue.callresult

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.simple.clinic.sync.DataPullResponse

@JsonClass(generateAdapter = true)
data class CallResultPullResponse(

    @Json(name = "call_results")
    override val payloads: List<CallResultPayload>,

    @Json(name = "process_token")
    override val processToken: String

) : DataPullResponse<CallResultPayload>
