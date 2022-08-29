package org.simple.clinic.summary.nextappointment

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.summary.PatientSummaryChildModel
import java.time.LocalDate
import java.util.UUID

@Parcelize
data class NextAppointmentModel(
    val patientUuid: UUID,
    val nextAppointmentPatientProfile: NextAppointmentPatientProfile?,
    val currentDate: LocalDate
) : Parcelable, PatientSummaryChildModel {

  companion object {
    fun default(
        patientUuid: UUID,
        currentDate: LocalDate
    ) = NextAppointmentModel(
        patientUuid = patientUuid,
        nextAppointmentPatientProfile = null,
        currentDate = currentDate
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

  override fun readyToRender(): Boolean {
    return true
  }
}
