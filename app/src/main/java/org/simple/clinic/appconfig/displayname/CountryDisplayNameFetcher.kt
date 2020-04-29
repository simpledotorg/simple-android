package org.simple.clinic.appconfig.displayname

import org.simple.clinic.appconfig.Country

interface CountryDisplayNameFetcher {

  fun displayNameForCountry(country: Country): String
}
