package org.simple.clinic.selectcountry

data class SelectCountryModel(
    val countries: List<Country>?
) {

  fun hasFetchedCountries(): Boolean = countries != null

  fun withCountries(countries: List<Country>): SelectCountryModel {
    return copy(countries = countries)
  }

  companion object {
    val FETCHING = SelectCountryModel(countries = null)
  }
}
