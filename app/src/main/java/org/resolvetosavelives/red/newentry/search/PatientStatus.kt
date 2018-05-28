package org.resolvetosavelives.red.newentry.search

import android.arch.persistence.room.TypeConverter
import com.squareup.moshi.Json

enum class PatientStatus {

  @Json(name = "active")
  ACTIVE,

  @Json(name = "dead")
  DEAD,

  @Json(name = "migrated")
  MIGRATED,

  @Json(name = "unresponsive")
  UNRESPONSIVE,

  @Json(name = "inactive")
  INACTIVE;

  class RoomTypeConverter {

    @TypeConverter
    fun fromEnum(status: PatientStatus): String {
      return status.name
    }

    @TypeConverter
    fun toEnum(status: String): PatientStatus {
      return PatientStatus.valueOf(status)
    }
  }
}
