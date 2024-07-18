package org.simple.clinic.selectcountry

import org.simple.clinic.appconfig.Country
import org.simple.clinic.widgets.UiEvent

sealed class SelectCountryEvent : UiEvent

data class ManifestFetched(val countries: List<Country>) : SelectCountryEvent()

data class ManifestFetchFailed(val error: ManifestFetchError) : SelectCountryEvent()

data class CountryChosen(val country: Country) : SelectCountryEvent() {
  override val analyticsName: String = "Select Country:Selected Country Changed:${country.isoCountryCode}"
}

data object CountrySaved : SelectCountryEvent()

data object RetryClicked : SelectCountryEvent() {
  override val analyticsName: String = "Select Country:Retry Clicked"
}
