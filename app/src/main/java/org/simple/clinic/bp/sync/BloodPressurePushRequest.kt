package org.simple.clinic.bp.sync

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BloodPressurePushRequest(

    @Json(name = "blood_pressures")
    val measurements: List<BloodPressureMeasurementPayload>
)
