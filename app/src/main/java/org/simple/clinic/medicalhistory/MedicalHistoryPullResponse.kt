package org.simple.clinic.medicalhistory

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.simple.clinic.sync.DataPullResponse
import org.threeten.bp.Instant

@JsonClass(generateAdapter = true)
data class MedicalHistoryPullResponse(

    @Json(name = "appointments")
    override val payloads: List<MedicalHistoryPayload>,

    @Json(name = "processed_since")
    override val processedSinceTimestamp: Instant

) : DataPullResponse<MedicalHistoryPayload>
