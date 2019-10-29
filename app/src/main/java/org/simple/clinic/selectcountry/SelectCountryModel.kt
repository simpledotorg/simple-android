package org.simple.clinic.selectcountry

data class SelectCountryModel(
    val supportedCountries: List<Country>?
) {

  fun hasFetchedCountries() = supportedCountries != null

  fun withSupportedCountries(countries: List<Country>): SelectCountryModel {
    return copy(supportedCountries = countries)
  }

  companion object {
    val FETCHING = SelectCountryModel(null)
  }
}
