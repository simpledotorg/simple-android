package org.simple.clinic.util

import com.f2prateek.rx.preferences2.Preference

class OptionalRxPreferencesConverter<T : Any>(private val valueConverter: Preference.Converter<T>) : Preference.Converter<Optional<T>> {

  override fun deserialize(serialized: String): Optional<T> {
    return when {
      serialized.isEmpty() -> None
      else -> Just(valueConverter.deserialize(serialized))
    }
  }

  override fun serialize(optional: Optional<T>): String {
    return when (optional) {
      is Just -> valueConverter.serialize(optional.value)
      is None -> ""
    }
  }
}
