package org.simple.clinic.selectcountry

import org.simple.clinic.appconfig.Country

data class SelectCountryModel(
    val countries: List<Country>?,
    val manifestFetchError: ManifestFetchError?,
    val selectedCountry: Country?
) {

  fun hasFetchedCountries(): Boolean = countries != null

  fun manifestFetched(countries: List<Country>): SelectCountryModel {
    return copy(countries = countries)
  }

  fun manifestFetchError(manifestFetchError: ManifestFetchError): SelectCountryModel {
    return copy(manifestFetchError = manifestFetchError)
  }

  fun countryChosen(country: Country): SelectCountryModel {
    return copy(selectedCountry = country)
  }

  fun fetching(): SelectCountryModel {
    return copy(countries = null, manifestFetchError = null)
  }

  companion object {
    val FETCHING = SelectCountryModel(
        countries = null,
        manifestFetchError = null,
        selectedCountry = null
    )
  }
}
