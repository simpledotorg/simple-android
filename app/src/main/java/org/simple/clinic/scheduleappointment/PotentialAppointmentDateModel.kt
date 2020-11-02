package org.simple.clinic.scheduleappointment

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.overdue.PotentialAppointmentDate

@Parcelize
data class PotentialAppointmentDateModel(
    val potentialAppointmentDates: List<PotentialAppointmentDate>,
    val selectedAppointmentDate: PotentialAppointmentDate?
) : Parcelable {

  companion object {
    fun create(
        potentialAppointmentDates: List<PotentialAppointmentDate>,
    ): PotentialAppointmentDateModel {
      return PotentialAppointmentDateModel(
          potentialAppointmentDates = potentialAppointmentDates,
          selectedAppointmentDate = null
      )
    }
  }

  fun selectedAppointmentDate(potentialAppointmentDate: PotentialAppointmentDate): PotentialAppointmentDateModel {
    return copy(selectedAppointmentDate = potentialAppointmentDate)
  }
}
