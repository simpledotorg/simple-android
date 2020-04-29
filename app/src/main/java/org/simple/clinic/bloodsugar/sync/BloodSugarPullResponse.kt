package org.simple.clinic.bloodsugar.sync

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.simple.clinic.sync.DataPullResponse

@JsonClass(generateAdapter = true)
data class BloodSugarPullResponse(

    @Json(name = "blood_sugars")
    override val payloads: List<BloodSugarMeasurementPayload>,

    @Json(name = "process_token")
    override val processToken: String

) : DataPullResponse<BloodSugarMeasurementPayload>
