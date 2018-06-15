package org.simple.clinic.util

import android.arch.persistence.room.TypeConverter
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.util.UUID

class UuidRoomTypeConverter {

  @TypeConverter
  fun toUuid(value: String?): UUID? {
    return value?.let { UUID.fromString(value) }
  }

  @TypeConverter
  fun fromUuid(uuid: UUID?): String? {
    return uuid?.toString()
  }
}

class UuidMoshiAdapter {

  @FromJson
  fun toUuid(value: String?): UUID? {
    return value?.let { UUID.fromString(it) }
  }

  @ToJson
  fun fromUuid(uuid: UUID?): String? {
    return uuid?.toString()
  }
}
