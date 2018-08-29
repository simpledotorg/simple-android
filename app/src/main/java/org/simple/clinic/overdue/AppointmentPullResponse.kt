package org.simple.clinic.overdue

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.simple.clinic.sync.DataPullResponse
import org.threeten.bp.Instant

@JsonClass(generateAdapter = true)
data class AppointmentPullResponse(

    @Json(name = "appointments")
    override val payloads: List<AppointmentPayload>,

    @Json(name = "processed_since")
    override val processedSinceTimestamp: Instant

) : DataPullResponse<AppointmentPayload>
