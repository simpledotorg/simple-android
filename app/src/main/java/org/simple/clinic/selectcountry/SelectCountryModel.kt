package org.simple.clinic.selectcountry

data class SelectCountryModel(
    val countries: List<Country>?,
    val manifestFetchError: ManifestFetchError?
) {

  fun hasFetchedCountries(): Boolean = countries != null

  fun withCountries(countries: List<Country>): SelectCountryModel {
    return copy(countries = countries)
  }

  fun manifestFetchError(manifestFetchError: ManifestFetchError): SelectCountryModel {
    return copy(manifestFetchError = manifestFetchError)
  }

  companion object {
    val FETCHING = SelectCountryModel(
        countries = null,
        manifestFetchError = null
    )
  }
}
