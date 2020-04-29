package org.simple.clinic.util.preference

import com.f2prateek.rx.preferences2.Preference
import com.squareup.moshi.Moshi

class MoshiObjectPreferenceConverter<T>(
    moshi: Moshi,
    clazz: Class<T>
) : Preference.Converter<T> {

  private val adapter = moshi.adapter(clazz)

  override fun deserialize(serialized: String): T {
    return adapter.fromJson(serialized)!!
  }

  override fun serialize(value: T): String {
    return adapter.toJson(value)
  }
}
