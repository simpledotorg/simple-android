package org.simple.clinic.util.room

import androidx.room.TypeConverter
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter

class LocalDateRoomTypeConverter {

  companion object {
    private val formatter = DateTimeFormatter.ISO_DATE!!
  }

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

