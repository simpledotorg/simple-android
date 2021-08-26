package org.simple.clinic.appconfig

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CountriesPayload(

    @Json(name = "countries")
    val countries: List<CountryV2>
)
