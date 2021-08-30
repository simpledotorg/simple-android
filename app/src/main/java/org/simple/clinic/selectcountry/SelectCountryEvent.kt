package org.simple.clinic.selectcountry

import org.simple.clinic.appconfig.CountryV2
import org.simple.clinic.widgets.UiEvent

sealed class SelectCountryEvent : UiEvent

data class ManifestFetched(val countries: List<CountryV2>) : SelectCountryEvent()

data class ManifestFetchFailed(val error: ManifestFetchError) : SelectCountryEvent()

data class CountryChosen(val country: CountryV2) : SelectCountryEvent() {
  override val analyticsName: String = "Select Country:Selected Country Changed:${country.isoCountryCode}"
}

object NextClicked : SelectCountryEvent() {
  override val analyticsName: String = "Select Country:Next Clicked"
}

object CountrySaved : SelectCountryEvent()

object RetryClicked : SelectCountryEvent() {
  override val analyticsName: String = "Select Country:Retry Clicked"
}

object DeploymentSaved : SelectCountryEvent()
