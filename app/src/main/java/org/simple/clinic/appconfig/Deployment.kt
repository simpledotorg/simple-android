package org.simple.clinic.appconfig

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.net.URI

@JsonClass(generateAdapter = true)
data class Deployment(
    @Json(name = "display_name")
    val displayName: String,

    @Json(name = "endpoint")
    val endPoint: URI
)
