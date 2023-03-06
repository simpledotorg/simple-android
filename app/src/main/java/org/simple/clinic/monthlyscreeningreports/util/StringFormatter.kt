package org.simple.clinic.monthlyscreeningreports.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun getScreeningMonth(
    content: Map<String, Any>,
    dateTimeFormatter: DateTimeFormatter
): String {
  return try {
    val date = formatScreeningMonthStringToLocalDate(content)
    return dateTimeFormatter.format(date)
  } catch (ex: Exception) {
    ""
  }
}

fun getScreeningSubmitStatus(content: Map<String, Any>): Boolean {
  return try {
    content["submitted"] as Boolean
  } catch (ex: Exception) {
    false
  }
}

fun formatScreeningMonthStringToLocalDate(content: Map<String, Any>): LocalDate? {
  return try {
    val monthString = content["month_date"] as String
    LocalDate.parse(monthString)
  } catch (ex: Exception) {
    null
  }
}
