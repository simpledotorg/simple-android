package org.simple.clinic.summary.teleconsultation.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TeleconsultationsResponse(
    @Json(name = "teleconsultation_phone_number")
    val teleconsultationPhoneNumber: String?
)
