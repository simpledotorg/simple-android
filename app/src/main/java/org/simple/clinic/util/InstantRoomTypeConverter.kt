package org.simple.clinic.util

import androidx.room.TypeConverter
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

