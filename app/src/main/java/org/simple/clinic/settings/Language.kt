package org.simple.clinic.settings

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.appconfig.Country
import java.util.Locale

sealed class Language : Parcelable

@Parcelize
data class ProvidedLanguage(val displayName: String, val languageCode: String) : Language() {

  fun matchesLocale(locale: Locale): Boolean {
    val languageTag = locale.toLanguageTag()

    return languageCode.equals(languageTag, ignoreCase = true)
  }

  fun isApplicableToCountry(country: Country): Boolean {
    val parts = languageCode.split("-")
    return when (parts.size) {
      1 -> true
      2 -> languageCode.contains(country.isoCountryCode, ignoreCase = true)
      else -> true
    }
  }

  fun toLocale(): Locale {
    return Locale.forLanguageTag(languageCode)
  }
}

@Parcelize
data object SystemDefaultLanguage : Language()
