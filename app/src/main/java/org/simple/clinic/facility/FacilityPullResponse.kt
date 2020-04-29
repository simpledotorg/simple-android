package org.simple.clinic.facility

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.simple.clinic.sync.DataPullResponse

@JsonClass(generateAdapter = true)
data class FacilityPullResponse(

    @Json(name = "facilities")
    override val payloads: List<FacilityPayload>,

    @Json(name = "process_token")
    override val processToken: String
) : DataPullResponse<FacilityPayload>
