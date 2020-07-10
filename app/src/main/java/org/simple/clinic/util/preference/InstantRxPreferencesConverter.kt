package org.simple.clinic.util.preference

import com.f2prateek.rx.preferences2.Preference
import java.time.Instant

class InstantRxPreferencesConverter : Preference.Converter<Instant> {

  override fun deserialize(value: String): Instant {
    return Instant.parse(value)
  }

  override fun serialize(value: Instant): String {
    return value.toString()
  }
}
