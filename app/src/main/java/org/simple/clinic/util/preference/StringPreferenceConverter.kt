package org.simple.clinic.util.preference

import com.f2prateek.rx.preferences2.Preference

class StringPreferenceConverter : Preference.Converter<String> {
  override fun serialize(value: String): String {
    return value
  }

  override fun deserialize(serialized: String): String {
    return serialized
  }
}
