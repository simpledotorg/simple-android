package org.simple.clinic.selectcountry

import org.simple.clinic.appconfig.Country

interface SelectCountryUi {
  fun showProgress()
  fun displaySupportedCountries(countries: List<Country>, chosenCountry: Country?)
  fun displayNetworkErrorMessage()
  fun displayServerErrorMessage()
  fun displayGenericErrorMessage()
}
