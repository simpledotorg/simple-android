package org.simple.clinic.medicalhistory.sync

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.simple.clinic.sync.DataPullResponse

@JsonClass(generateAdapter = true)
data class MedicalHistoryPullResponse(

    @Json(name = "medical_histories")
    override val payloads: List<MedicalHistoryPayload>,

    @Json(name = "process_token")
    override val processToken: String

) : DataPullResponse<MedicalHistoryPayload>
