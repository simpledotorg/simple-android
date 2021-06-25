package org.simple.clinic.patient.onlinelookup.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PatientOnlineLookupRequest(

    @Json(name = "identifier")
    val identifier: String
)
