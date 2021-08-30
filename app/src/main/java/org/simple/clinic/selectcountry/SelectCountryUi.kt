package org.simple.clinic.selectcountry

import org.simple.clinic.appconfig.CountryV2

interface SelectCountryUi {
  fun showProgress()
  fun displaySupportedCountries(countries: List<CountryV2>, chosenCountry: CountryV2?)
  fun displayNetworkErrorMessage()
  fun displayServerErrorMessage()
  fun displayGenericErrorMessage()
  fun showNextButton()
}
