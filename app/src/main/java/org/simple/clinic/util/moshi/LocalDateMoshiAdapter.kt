package org.simple.clinic.util.moshi

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class LocalDateMoshiAdapter {

  companion object {
    private val formatter = DateTimeFormatter.ISO_DATE!!
  }

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
