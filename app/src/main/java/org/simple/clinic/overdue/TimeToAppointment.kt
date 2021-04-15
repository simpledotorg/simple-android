package org.simple.clinic.overdue

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class TimeToAppointment(val value: Int): Parcelable {

  @Parcelize
  data class Days(val daysTillAppointment: Int) : TimeToAppointment(daysTillAppointment)

  @Parcelize
  data class Weeks(val weeksTillAppointment: Int) : TimeToAppointment(weeksTillAppointment)

  @Parcelize
  data class Months(val monthsTillAppointment: Int) : TimeToAppointment(monthsTillAppointment)
}
