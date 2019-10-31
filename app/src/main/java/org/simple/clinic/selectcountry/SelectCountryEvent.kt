package org.simple.clinic.selectcountry

import org.simple.clinic.appconfig.Country

sealed class SelectCountryEvent

data class ManifestFetched(val countries: List<Country>) : SelectCountryEvent()

data class ManifestFetchFailed(val error: ManifestFetchError) : SelectCountryEvent()

data class CountryChosen(val country: Country) : SelectCountryEvent()

object NextClicked : SelectCountryEvent()

object CountrySaved : SelectCountryEvent()

object RetryClicked : SelectCountryEvent()
