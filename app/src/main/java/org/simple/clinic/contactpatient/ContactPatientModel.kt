package org.simple.clinic.contactpatient

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.contactpatient.ContactPatientInfoProgressState.DONE
import org.simple.clinic.contactpatient.ContactPatientInfoProgressState.IN_PROGRESS
import org.simple.clinic.facility.Facility
import org.simple.clinic.overdue.Appointment
import org.simple.clinic.overdue.AppointmentConfig
import org.simple.clinic.overdue.PotentialAppointmentDate
import org.simple.clinic.overdue.callresult.CallResult
import org.simple.clinic.patient.PatientStatus
import org.simple.clinic.util.ParcelableOptional
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.parcelable
import java.time.LocalDate
import java.util.Optional
import java.util.UUID

@Parcelize
data class ContactPatientModel(
    val patientUuid: UUID,
    val uiMode: UiMode,
    val patientProfile: ContactPatientProfile? = null,
    val overdueAppointment: ParcelableOptional<Appointment>? = null,
    val secureCallingFeatureEnabled: Boolean,
    val potentialAppointments: List<PotentialAppointmentDate>,
    val selectedAppointmentDate: LocalDate,
    val selectedRemoveAppointmentReason: RemoveAppointmentReason?,
    val contactPatientInfoProgressState: ContactPatientInfoProgressState?,
    val currentFacility: Facility? = null,
    val callResult: ParcelableOptional<CallResult>?
) : Parcelable {

  companion object {
    fun create(
        patientUuid: UUID,
        appointmentConfig: AppointmentConfig,
        userClock: UserClock,
        mode: UiMode,
        secureCallFeatureEnabled: Boolean
    ): ContactPatientModel {
      val potentialAppointments = PotentialAppointmentDate.from(appointmentConfig.remindAppointmentsIn, userClock)

      return ContactPatientModel(
          patientUuid = patientUuid,
          uiMode = mode,
          secureCallingFeatureEnabled = secureCallFeatureEnabled,
          potentialAppointments = potentialAppointments,
          selectedAppointmentDate = potentialAppointments.first().scheduledFor,
          selectedRemoveAppointmentReason = null,
          contactPatientInfoProgressState = null,
          callResult = null
      )
    }
  }

  val hasPatientDied: Boolean
    get() = patientProfile?.patient?.status == PatientStatus.Dead

  val hasCurrentFacility: Boolean
    get() = currentFacility != null

  val hasLoadedPatientProfile: Boolean
    get() = patientProfile != null

  val patientProfileHasPhoneNumber: Boolean
    get() = !patientProfile?.phoneNumbers.isNullOrEmpty()

  val hasLoadedOverdueAppointment: Boolean
    get() = overdueAppointment != null

  val isOverdueAppointmentPresent: Boolean
    get() = overdueAppointment?.isPresent() == true

  val hasRegisteredFacility: Boolean
    get() = patientProfile != null && patientProfile.patient.registeredFacilityId != null

  val patientIsAtRegisteredFacility: Boolean
    get() = patientProfile != null && patientProfile.patient.registeredFacilityId == currentFacility?.uuid

  val appointment: Appointment
    get() = overdueAppointment!!.get()

  val appointmentUuid: UUID
    get() = appointment.uuid

  val isPatientContactInfoLoaded: Boolean
    get() = contactPatientInfoProgressState == DONE

  val hasCallResult: Boolean
    get() = callResult != null && callResult.isPresent()

  fun contactPatientProfileLoaded(contactPatientProfile: ContactPatientProfile): ContactPatientModel {
    return copy(patientProfile = contactPatientProfile)
  }

  fun overdueAppointmentLoaded(appointment: Optional<Appointment>): ContactPatientModel {
    return copy(overdueAppointment = appointment.parcelable())
  }

  fun reminderDateSelected(date: PotentialAppointmentDate): ContactPatientModel {
    return copy(selectedAppointmentDate = date.scheduledFor)
  }

  fun changeUiModeTo(newMode: UiMode): ContactPatientModel {
    return copy(uiMode = newMode)
  }

  fun contactPatientInfoLoaded(): ContactPatientModel {
    return copy(contactPatientInfoProgressState = DONE)
  }

  fun contactPatientInfoLoading(): ContactPatientModel {
    return copy(contactPatientInfoProgressState = IN_PROGRESS)
  }

  fun currentFacilityLoaded(currentFacility: Facility): ContactPatientModel {
    return copy(currentFacility = currentFacility)
  }

  fun callResultLoaded(callResult: Optional<CallResult>): ContactPatientModel {
    return copy(callResult = callResult.parcelable())
  }
}
