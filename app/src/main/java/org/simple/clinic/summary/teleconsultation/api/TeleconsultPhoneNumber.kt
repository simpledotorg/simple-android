package org.simple.clinic.summary.teleconsultation.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TeleconsultPhoneNumber(
    @Json(name = "phone_number")
    val phoneNumber: String
)
