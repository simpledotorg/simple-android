package org.simple.clinic.sync

import com.f2prateek.rx.preferences2.Preference

class EnumRxPreferenceConverter<T : Enum<T>>(private val enumClass: Class<T>) : Preference.Converter<T> {

  override fun deserialize(serialized: String): T {
    return java.lang.Enum.valueOf(asEnumClass<T>(enumClass), serialized)
  }

  override fun serialize(value: T): String {
    return value.name
  }

  /* This weirdness https://discuss.kotlinlang.org/t/confusing-type-error/506/8 */
  @Suppress("NOTHING_TO_INLINE", "UNCHECKED_CAST")
  private inline fun <T : Enum<T>> asEnumClass(clazz: Class<*>): Class<T> = clazz as Class<T>
}
