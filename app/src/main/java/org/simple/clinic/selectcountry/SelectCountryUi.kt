package org.simple.clinic.selectcountry

import org.simple.clinic.appconfig.Country

interface SelectCountryUi {
  fun showProgress()
  fun hideProgress()
  fun displaySupportedCountries(countries: List<Country>)
  fun displayNetworkErrorMessage()
  fun displayServerErrorMessage()
  fun displayGenericErrorMessage()
  fun showRetryButton()
}
