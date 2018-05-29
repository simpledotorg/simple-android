package org.resolvetosavelives.red.util

import com.f2prateek.rx.preferences2.Preference
import com.gojuno.koptional.None
import com.gojuno.koptional.Optional
import com.gojuno.koptional.Some

class OptionalRxPreferencesConverter<T : Any>(private val valueConverter: Preference.Converter<T>) : Preference.Converter<Optional<T>> {

  override fun deserialize(serialized: String): Optional<T> {
    return when {
      serialized.isEmpty() -> None
      else -> Some(valueConverter.deserialize(serialized))
    }
  }

  override fun serialize(optional: Optional<T>): String {
    return when (optional) {
      is Some -> valueConverter.serialize(optional.value)
      is None -> ""
    }
  }
}
