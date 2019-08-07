package org.simple.clinic.scheduleappointment

import org.threeten.bp.LocalDate
import org.threeten.bp.temporal.ChronoUnit

sealed class TimeToAppointment(val value: Int) {

  companion object {
    fun from(currentDate: LocalDate, appointmentDate: LocalDate): TimeToAppointment {
      if (currentDate >= appointmentDate) {
        throw IllegalArgumentException("Appointment date ($appointmentDate) must be > current date ($currentDate)!")
      }

      val periodBetweenDates = currentDate.until(appointmentDate)
      val yearsInPeriod = periodBetweenDates.years
      val monthsRemainingInPeriod = periodBetweenDates.months
      val daysRemainingInPeriod = periodBetweenDates.days
      val totalDaysInPeriod = ChronoUnit.DAYS.between(currentDate, appointmentDate).toInt()
      val isExactlyOneYear = yearsInPeriod == 1 && monthsRemainingInPeriod == 0 && daysRemainingInPeriod == 0
      val isLessThanAYear = yearsInPeriod < 1

      return when {
        isExactlyOneYear -> Months(12)
        isLessThanAYear -> timeForPeriodLessThanAYear(monthsRemainingInPeriod, daysRemainingInPeriod, totalDaysInPeriod)
        else -> Days(totalDaysInPeriod)
      }
    }

    private fun timeForPeriodLessThanAYear(
        months: Int,
        days: Int,
        totalDaysInPeriod: Int
    ): TimeToAppointment {
      val isLessThanAMonth = months == 0
      return if (isLessThanAMonth) {
        timeForPeriodLessThanAMonth(days, totalDaysInPeriod)
      } else {
        timeForAPeriodMoreThanAMonth(days, months, totalDaysInPeriod)
      }
    }

    private fun timeForPeriodLessThanAMonth(
        daysRemainingInPeriod: Int,
        totalNumberOfDaysTillAppointment: Int
    ): TimeToAppointment {
      val daysInWeek = 7
      val doesPeriodContainExactWeeks = ((daysRemainingInPeriod % daysInWeek) == 0)
      val numberOfWeeksInPeriod = daysRemainingInPeriod / daysInWeek

      val numberOfWeeksToShowAsWeeks = 2..3
      return if (doesPeriodContainExactWeeks && numberOfWeeksInPeriod in numberOfWeeksToShowAsWeeks) {
        Weeks(numberOfWeeksInPeriod)
      } else {
        Days(totalNumberOfDaysTillAppointment)
      }
    }

    private fun timeForAPeriodMoreThanAMonth(
        daysRemainingInPeriod: Int,
        monthsRemainingInPeriod: Int,
        totalNumberOfDaysTillAppointment: Int
    ): TimeToAppointment {
      val doesPeriodContainExactMonths = (daysRemainingInPeriod == 0)

      return if (doesPeriodContainExactMonths) {
        Months(monthsRemainingInPeriod)
      } else {
        Days(totalNumberOfDaysTillAppointment)
      }
    }
  }

  data class Days(val daysTillAppointment: Int) : TimeToAppointment(daysTillAppointment)

  data class Weeks(val weeksTillAppointment: Int) : TimeToAppointment(weeksTillAppointment)

  data class Months(val monthsTillAppointment: Int) : TimeToAppointment(monthsTillAppointment)
}
