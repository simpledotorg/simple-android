package org.simple.clinic.appconfig

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Manifest(

    @Json(name = "v2")
    val supportedCountries: CountriesPayload
)
