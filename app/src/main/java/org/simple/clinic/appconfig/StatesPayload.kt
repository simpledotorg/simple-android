package org.simple.clinic.appconfig

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class StatesPayload(

    @Json(name = "states")
    val states: List<StatePayload>
)
