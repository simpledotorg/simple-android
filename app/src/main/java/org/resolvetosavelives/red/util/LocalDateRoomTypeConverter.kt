package org.resolvetosavelives.red.util

import android.arch.persistence.room.TypeConverter
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter

class LocalDateRoomTypeConverter {

  private val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")

  @TypeConverter
  fun toLocalDate(value: String?): LocalDate? {
    return value?.let {
      return formatter.parse(value, LocalDate::from)
    }
  }

  @TypeConverter
  fun fromLocalDate(date: LocalDate?): String? {
    return date?.format(formatter)
  }
}
