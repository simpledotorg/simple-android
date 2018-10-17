package org.simple.clinic.facility

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.simple.clinic.sync.DataPullResponse
import org.threeten.bp.Instant

@JsonClass(generateAdapter = true)
data class FacilityPullResponse(

    @Json(name = "facilities")
    override val payloads: List<FacilityPayload>,

    @Json(name = "processed_since")
    override val processedSinceTimestamp: Instant
) : DataPullResponse<FacilityPayload>
