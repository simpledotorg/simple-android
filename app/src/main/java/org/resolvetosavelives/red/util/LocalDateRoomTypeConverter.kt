package org.resolvetosavelives.red.util

import android.arch.persistence.room.TypeConverter
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter

class LocalDateRoomTypeConverter {

  private val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

  @TypeConverter
  fun toOffsetDateTime(value: String?): LocalDate? {
    return value?.let {
      return formatter.parse(value, LocalDate::from)
    }
  }

  @TypeConverter
  fun fromOffsetDateTime(date: LocalDate?): String? {
    return date?.format(formatter)
  }
}
