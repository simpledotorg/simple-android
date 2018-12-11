package org.simple.clinic.protocolv2

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.simple.clinic.protocolv2.sync.ProtocolPayload
import org.simple.clinic.sync.DataPullResponse

@JsonClass(generateAdapter = true)
data class ProtocolPullResponse(

    @Json(name = "protocols")
    override val payloads: List<ProtocolPayload>,

    @Json(name = "process_token")
    override val processedSinceTimestamp: String

) : DataPullResponse<ProtocolPayload>
