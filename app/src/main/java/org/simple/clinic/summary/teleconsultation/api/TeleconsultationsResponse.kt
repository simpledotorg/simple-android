package org.simple.clinic.summary.teleconsultation.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TeleconsultationsResponse(
    @Deprecated(message = "Use teleconsultationPhoneNumbers to get list of all MO phone numbers")
    @Json(name = "teleconsultation_phone_number")
    val teleconsultationPhoneNumber: String?,

    @Json(name = "teleconsultation_phone_numbers")
    val teleconsultationPhoneNumbers: List<TeleconsultPhoneNumber>?
)
