package org.simple.clinic.util

import android.arch.persistence.room.TypeConverter
import com.f2prateek.rx.preferences2.Preference
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import org.threeten.bp.Instant

class InstantRoomTypeConverter {

  @TypeConverter
  fun toInstant(value: String?): Instant? {
    return value?.let {
      return Instant.parse(value)
    }
  }

  @TypeConverter
  fun fromInstant(instant: Instant?): String? {
    return instant?.toString()
  }
}

class InstantMoshiAdapter {

  @FromJson
  fun toInstant(value: String?): Instant? {
    return value?.let {
      return Instant.parse(value)
    }
  }

  @ToJson
  fun fromInstant(instant: Instant?): String? {
    return instant?.toString()
  }
}

class InstantRxPreferencesConverter : Preference.Converter<Instant> {

  override fun deserialize(value: String): Instant {
    return Instant.parse(value)
  }

  override fun serialize(value: Instant): String {
    return value.toString()
  }
}
