package org.simple.clinic.scanid

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class IndiaNHIDDateOfBirthMoshiAdapter {
  private val dateTimeFormatter = DateTimeFormatter.ofPattern("d/M/yyyy", Locale.ENGLISH)

  @FromJson
  @IndiaNHIDDateOfBirth
  fun toLocalDate(value: String?): LocalDate? {
    return value?.let {
      dateTimeFormatter.parse(value, LocalDate::from)
    }
  }

  @ToJson
  fun fromLocalDate(@IndiaNHIDDateOfBirth date: LocalDate?): String? {
    return date?.format(dateTimeFormatter)
  }
}