package org.resolvetosavelives.red.newentry.search

import android.arch.persistence.room.TypeConverter

enum class PatientMobileNumberType {
  MOBILE,
  LANDLINE;

  class RoomTypeConverter {
    @TypeConverter
    fun fromEnum(type: PatientMobileNumberType): String {
      return type.name
    }

    @TypeConverter
    fun toEnum(type: String): PatientMobileNumberType {
      return PatientMobileNumberType.valueOf(type)
    }
  }
}
