package org.simple.clinic.encounter.sync

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.simple.clinic.sync.DataPullResponse

@JsonClass(generateAdapter = true)
data class EncounterPullResponse(

    @Json(name = "encounters")
    override val payloads: List<EncounterPayload>,

    @Json(name = "process_token")
    override val processToken: String

) : DataPullResponse<EncounterPayload>
