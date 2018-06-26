package org.simple.clinic.facility

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.threeten.bp.Instant

@JsonClass(generateAdapter = true)
data class FacilityPullResponse(

    @Json(name = "facilities")
    val facilities: List<FacilityPayload>,

    @Json(name = "processed_since")
    val processedSinceTimestamp: Instant
)
