package org.resolvetosavelives.red.util

import android.arch.persistence.room.TypeConverter
import org.threeten.bp.Instant

class InstantRoomTypeConverter {

  @TypeConverter
  fun toOffsetDateTime(value: String?): Instant? {
    return value?.let {
      return Instant.parse(value)
    }
  }

  @TypeConverter
  fun fromOffsetDateTime(date: Instant?): String? {
    return date?.toString()
  }
}
