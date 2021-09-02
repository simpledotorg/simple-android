package org.simple.clinic.appconfig

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class State(

    @Json(name = "display_name")
    val displayName: String,

    @Json(name = "deployment")
    val deployment: Deployment
)
