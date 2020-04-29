package org.simple.clinic.patient.sync

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.simple.clinic.sync.DataPullResponse

@JsonClass(generateAdapter = true)
data class PatientPullResponse(

    @Json(name = "patients")
    override val payloads: List<PatientPayload>,

    @Json(name = "process_token")
    override val processToken: String
) : DataPullResponse<PatientPayload>
