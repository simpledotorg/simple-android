package org.resolvetosavelives.red.newentry.search

import android.arch.persistence.room.TypeConverter

enum class PatientPhoneNumberType {
  MOBILE,
  LANDLINE;

  class RoomTypeConverter {
    @TypeConverter
    fun fromEnum(type: PatientPhoneNumberType): String {
      return type.name
    }

    @TypeConverter
    fun toEnum(type: String): PatientPhoneNumberType {
      return PatientPhoneNumberType.valueOf(type)
    }
  }
}
