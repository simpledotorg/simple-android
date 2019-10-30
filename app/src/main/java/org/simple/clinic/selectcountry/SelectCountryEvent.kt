package org.simple.clinic.selectcountry

sealed class SelectCountryEvent

data class ManifestFetched(val countries: List<Country>) : SelectCountryEvent()
