package org.simple.clinic.summary.teleconsultation.sync

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.simple.clinic.sync.DataPullResponse

@JsonClass(generateAdapter = true)
data class TeleconsultFacilityInfoPullResponse(
    @Json(name = "teleconsultation_medical_officers")
    override val payloads: List<TeleconsultationFacilityInfoPayload>,

    @Json(name = "process_token")
    override val processToken: String
) : DataPullResponse<TeleconsultationFacilityInfoPayload>
