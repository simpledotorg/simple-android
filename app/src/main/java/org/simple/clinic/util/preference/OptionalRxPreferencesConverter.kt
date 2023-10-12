package org.simple.clinic.util.preference

import com.f2prateek.rx.preferences2.Preference
import java.util.Optional

class OptionalRxPreferencesConverter<T>(private val valueConverter: Preference.Converter<T>) : Preference.Converter<Optional<T>> {

  override fun deserialize(serialized: String): Optional<T> {
    return when {
      serialized.isEmpty() -> Optional.empty<T>() as Optional<T>
      else -> Optional.of<T>(valueConverter.deserialize(serialized)) as Optional<T>
    }
  }

  override fun serialize(optional: Optional<T>): String {
    return optional
        .map { valueConverter.serialize(it!!) }
        .orElse("")
  }
}
