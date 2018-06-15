package org.simple.clinic.patient.sync

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.threeten.bp.Instant

@JsonClass(generateAdapter = true)
data class PatientPullResponse(

    @Json(name = "patients")
    val patients: List<PatientPayload>,

    @Json(name = "processed_since")
    val processedSinceTimestamp: Instant
)
