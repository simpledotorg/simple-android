package org.simple.clinic.user

import com.f2prateek.rx.preferences2.Preference
import com.squareup.moshi.JsonAdapter

class LoggedInUserRxPreferencesConverter(

    private val jsonAdapter: JsonAdapter<LoggedInUser>

) : Preference.Converter<LoggedInUser> {

  override fun deserialize(serialized: String): LoggedInUser {
    return jsonAdapter.fromJson(serialized)!!
  }

  override fun serialize(value: LoggedInUser): String {
    return jsonAdapter.toJson(value)
  }
}
