package org.simple.clinic.summary.teleconsultation.sync

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TeleconsultFacilityInfoPullResponse(
    @Json(name = "facility_medical_officers")
    val payloads: List<TeleconsultationFacilityInfoPayload>,
)
