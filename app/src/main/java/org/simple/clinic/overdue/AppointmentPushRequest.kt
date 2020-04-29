package org.simple.clinic.overdue

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AppointmentPushRequest(

    @Json(name = "appointments")
    val appointments: List<AppointmentPayload>
)
