package org.simple.clinic.util.preference

import com.f2prateek.rx.preferences2.Preference
import java.util.Locale

class LocalePreferenceConverter : Preference.Converter<Locale> {

  override fun deserialize(serialized: String): Locale {
    return Locale.forLanguageTag(serialized)
  }

  override fun serialize(value: Locale): String {
    return value.toLanguageTag()
  }
}
