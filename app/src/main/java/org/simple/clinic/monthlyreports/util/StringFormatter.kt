package org.simple.clinic.monthlyreports.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun getMonthlyReportFormattedMonthString(
    content: Map<String, Any?>,
    dateTimeFormatter: DateTimeFormatter
): String {
  return try {
    val date = parseMonthlyReportMonthStringToLocalDate(content)
    return dateTimeFormatter.format(date)
  } catch (ex: Exception) {
    ""
  }
}

fun getMonthlyReportSubmitStatus(content: Map<String, Any?>): Boolean {
  return try {
    content["submitted"] as Boolean
  } catch (ex: Exception) {
    false
  }
}

fun parseMonthlyReportMonthStringToLocalDate(content: Map<String, Any?>): LocalDate? {
  return try {
    val monthString = content["month_date"] as String
    LocalDate.parse(monthString)
  } catch (ex: Exception) {
    null
  }
}
