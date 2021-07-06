package org.simple.clinic.patient.onlinelookup.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OnlineLookupResponsePayload(

    @Json(name = "patients")
    val patients: List<CompleteMedicalRecordPayload>
)
