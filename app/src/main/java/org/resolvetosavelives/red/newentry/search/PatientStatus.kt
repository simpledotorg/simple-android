package org.resolvetosavelives.red.newentry.search

import android.arch.persistence.room.TypeConverter

enum class PatientStatus {
  ACTIVE,
  DEAD,
  MIGRATED,
  UNRESPONSIVE,
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
