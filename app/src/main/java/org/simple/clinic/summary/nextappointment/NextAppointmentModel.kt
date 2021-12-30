package org.simple.clinic.summary.nextappointment

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
data class NextAppointmentModel(
    val patientUuid: UUID,
    val nextAppointmentPatientProfile: NextAppointmentPatientProfile?
) : Parcelable {

  companion object {
    fun default(patientUuid: UUID) = NextAppointmentModel(
        patientUuid = patientUuid,
        nextAppointmentPatientProfile = null
    )
  }

  val hasNextAppointmentPatientProfile
    get() = nextAppointmentPatientProfile != null

  val appointment
    get() = nextAppointmentPatientProfile!!.appointment!!

  val appointmentIsInAssignedFacility
    get() = appointment.facilityUuid == nextAppointmentPatientProfile!!.patient.assignedFacilityId

  val appointmentFacilityName
    get() = nextAppointmentPatientProfile!!.facility.name

  fun nextAppointmentPatientProfileLoaded(nextAppointmentPatientProfile: NextAppointmentPatientProfile?): NextAppointmentModel {
    return copy(nextAppointmentPatientProfile = nextAppointmentPatientProfile)
  }
}
