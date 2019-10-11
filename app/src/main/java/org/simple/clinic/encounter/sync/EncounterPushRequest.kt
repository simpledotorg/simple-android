package org.simple.clinic.encounter.sync

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class EncounterPushRequest(

    @Json(name = "encounters")
    val encounters: List<EncounterPayload>
)
