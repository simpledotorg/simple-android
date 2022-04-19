package org.simple.clinic.util.preference

import com.f2prateek.rx.preferences2.Preference

class BooleanPreferenceConverter : Preference.Converter<Boolean> {

  override fun deserialize(serialized: String): Boolean {
    return serialized.toBoolean()
  }

  override fun serialize(value: Boolean): String {
    return value.toString()
  }
}
