package org.simple.clinic.util.preference

import com.f2prateek.rx.preferences2.Preference
import org.simple.clinic.util.Just
import org.simple.clinic.util.None
import org.simple.clinic.util.Optional

class OptionalRxPreferencesConverter<T>(private val valueConverter: Preference.Converter<T>) : Preference.Converter<Optional<T>> {

  override fun deserialize(serialized: String): Optional<T> {
    return when {
      serialized.isEmpty() -> None()
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
