package org.resolvetosavelives.red.newentry.search

import android.arch.persistence.room.TypeConverter
import com.squareup.moshi.Json

enum class Gender {

  @Json(name = "male")
  MALE,

  @Json(name = "female")
  FEMALE,

  @Json(name = "transgender")
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
