package org.simple.clinic.selectcountry

import java.net.URI

data class Country(
    val code: String,
    val endpoint: URI,
    val displayName: String,
    val isdCode: String
)
