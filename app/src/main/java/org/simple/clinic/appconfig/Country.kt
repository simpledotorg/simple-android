package org.simple.clinic.appconfig

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.net.URI

@JsonClass(generateAdapter = true)
data class Country(

    @Json(name = "country_code")
    val code: String,

    @Json(name = "endpoint")
    val endpoint: URI,

    @Json(name = "display_name")
    val displayName: String,

    @Json(name = "isd_code")
    val isdCode: String
)
