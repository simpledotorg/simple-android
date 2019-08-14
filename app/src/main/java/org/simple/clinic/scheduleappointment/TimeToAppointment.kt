package org.simple.clinic.scheduleappointment

sealed class TimeToAppointment(val value: Int) {
  
  data class Days(val daysTillAppointment: Int) : TimeToAppointment(daysTillAppointment)

  data class Weeks(val weeksTillAppointment: Int) : TimeToAppointment(weeksTillAppointment)

  data class Months(val monthsTillAppointment: Int) : TimeToAppointment(monthsTillAppointment)
}
