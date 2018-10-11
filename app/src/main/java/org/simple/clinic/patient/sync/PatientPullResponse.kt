package org.simple.clinic.patient.sync

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.simple.clinic.sync.DataPullResponse
import org.threeten.bp.Instant

@JsonClass(generateAdapter = true)
data class PatientPullResponse(

    @Json(name = "patients")
    override val payloads: List<PatientPayload>,

    @Json(name = "processed_since")
    override val processedSinceTimestamp: Instant
) : DataPullResponse<PatientPayload>
