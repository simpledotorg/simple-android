package org.simple.clinic.appconfig.displayname

import android.content.res.Resources
import androidx.annotation.StringRes
import org.simple.clinic.R
import org.simple.clinic.appconfig.Country
import javax.inject.Inject

class PlatformCountryDisplayNameFetcher @Inject constructor(
    private val resources: Resources
) : CountryDisplayNameFetcher {

  override fun displayNameForCountry(country: Country): String {
    return when (country.isoCountryCode) {
      "IN" -> string(R.string.country_india)
      "BD" -> string(R.string.country_bangladesh)
      "ET" -> string(R.string.country_ethiopia)
      else -> country.displayName
    }
  }

  private fun string(@StringRes id: Int): String {
    return resources.getString(id)
  }
}
