package org.simple.clinic.util.room

import androidx.room.TypeConverter
import java.time.Instant
import javax.inject.Inject

class InstantRoomTypeConverter @Inject constructor() {

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

