package org.simple.clinic.appconfig

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Manifest(

    @Json(name = "v1")
    val supportedCountriesOld: List<Country_Old>,

    @Json(name = "v2")
    val supportedCountriesV2: CountriesPayload
)
