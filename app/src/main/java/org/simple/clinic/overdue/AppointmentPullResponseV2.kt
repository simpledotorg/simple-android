package org.simple.clinic.overdue

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.simple.clinic.sync.DataPullResponse

@JsonClass(generateAdapter = true)
data class AppointmentPullResponseV2(

    @Json(name = "appointments")
    override val payloads: List<AppointmentPayload>,

    @Json(name = "process_token")
    override val processedSinceTimestamp: String

) : DataPullResponse<AppointmentPayload>
