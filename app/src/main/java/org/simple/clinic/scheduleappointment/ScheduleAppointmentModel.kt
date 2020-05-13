package org.simple.clinic.scheduleappointment

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class ScheduleAppointmentModel : Parcelable {

  companion object {
    fun create(): ScheduleAppointmentModel = ScheduleAppointmentModel()
  }
}
