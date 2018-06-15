package org.simple.clinic.bp.sync

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.threeten.bp.Instant

@JsonClass(generateAdapter = true)
data class BloodPressurePullResponse(

    @Json(name = "blood_pressures")
    val measurements: List<BloodPressureMeasurementPayload>,

    @Json(name = "processed_since")
    val processedSinceTimestamp: Instant
)
