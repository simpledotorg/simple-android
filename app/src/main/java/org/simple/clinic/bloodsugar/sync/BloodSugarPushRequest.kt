package org.simple.clinic.bloodsugar.sync

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BloodSugarPushRequest(

    @Json(name = "blood_sugars")
    val measurements: List<BloodSugarMeasurementPayload>
)
