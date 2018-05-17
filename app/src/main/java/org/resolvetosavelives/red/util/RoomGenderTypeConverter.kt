package org.resolvetosavelives.red.util

import android.arch.persistence.room.TypeConverter
import org.resolvetosavelives.red.newentry.search.Gender

class RoomGenderTypeConverter {

  @TypeConverter
  fun fromEnum(gender: Gender): String {
    return gender.name
  }

  @TypeConverter
  fun toEnum(gender: String): Gender {
    return Gender.valueOf(gender)
  }
}
