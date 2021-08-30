package org.simple.clinic.appconfig.displayname

import org.simple.clinic.appconfig.CountryV2

interface CountryDisplayNameFetcher {

  fun displayNameForCountry(country: CountryV2): String
}
