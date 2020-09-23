package org.simple.clinic.util.preference

import com.f2prateek.rx.preferences2.Preference
import com.f2prateek.rx.preferences2.RxSharedPreferences
import org.simple.clinic.util.Optional

fun <T> RxSharedPreferences.getOptional(
    key: String,
    converter: Preference.Converter<T>,
    defaultValue: Optional<T> = Optional.empty()
): Preference<Optional<T>> {
  return getObject(key, defaultValue, OptionalRxPreferencesConverter(converter))
}
