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

class UuidListRoomTypeConverter {

  private val uuidConverter = UuidRoomTypeConverter()

  @TypeConverter
  fun toUuids(value: String?): List<UUID>? {
    if (value == null) {
      return null
    }

    return value.split(delimiters = *arrayOf(","))
        .map { uuidConverter.toUuid(it)!! }
        .toList()
  }

  @TypeConverter
  fun fromUuids(uuids: List<UUID>?): String? {
    if (uuids == null) {
      return null
    }
    return uuids
        .map { uuidConverter.fromUuid(it) }
        .joinToString(separator = ",")
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
