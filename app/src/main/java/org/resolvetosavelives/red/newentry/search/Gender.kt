package org.resolvetosavelives.red.newentry.search

import android.arch.persistence.room.TypeConverter

enum class Gender {
  MALE,
  FEMALE,
  TRANSGENDER;

  class RoomTypeConverter {

    @TypeConverter
    fun fromEnum(gender: Gender): String {
      return gender.name
    }

    @TypeConverter
    fun toEnum(gender: String): Gender {
      return Gender.valueOf(gender)
    }
  }
}
