package org.resolvetosavelives.red.util

import android.arch.persistence.room.TypeConverter
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter

class LocalDateRoomTypeConverter {

  companion object {
    // TODO: Use DateTimeFormatter.ISO_DATE
    val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")!!
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

class LocalDateMoshiAdapter {

  private val formatter = DateTimeFormatter.ISO_DATE

  @FromJson
  fun toLocalDate(value: String?): LocalDate? {
    return value?.let {
      return formatter.parse(value, LocalDate::from)
    }
  }

  @ToJson
  fun fromLocalDate(date: LocalDate?): String? {
    return date?.format(formatter)
  }
}
