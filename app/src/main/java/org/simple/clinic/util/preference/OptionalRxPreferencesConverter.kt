package org.simple.clinic.util.preference

import com.f2prateek.rx.preferences2.Preference
import org.simple.clinic.util.Optional

class OptionalRxPreferencesConverter<T>(private val valueConverter: Preference.Converter<T>) : Preference.Converter<Optional<T>> {

  override fun deserialize(serialized: String): Optional<T> {
    return when {
      serialized.isEmpty() -> Optional.empty()
      else -> Optional.of(valueConverter.deserialize(serialized))
    }
  }

  override fun serialize(optional: Optional<T>): String {
    return optional
        .map { valueConverter.serialize(it!!) }
        .orElse("")
  }
}
