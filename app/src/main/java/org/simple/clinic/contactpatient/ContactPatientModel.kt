package org.simple.clinic.contactpatient

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.contactpatient.ContactPatientInfoProgressState.DONE
import org.simple.clinic.contactpatient.ContactPatientInfoProgressState.IN_PROGRESS
import org.simple.clinic.facility.Facility
import org.simple.clinic.home.overdue.OverdueAppointment
import org.simple.clinic.overdue.AppointmentConfig
import org.simple.clinic.overdue.PotentialAppointmentDate
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
    val appointment: ParcelableOptional<OverdueAppointment>? = null,
    val secureCallingFeatureEnabled: Boolean,
    val potentialAppointments: List<PotentialAppointmentDate>,
    val selectedAppointmentDate: LocalDate,
    val selectedRemoveAppointmentReason: RemoveAppointmentReason?,
    val contactPatientInfoProgressState: ContactPatientInfoProgressState?,
    val overdueListChangesFeatureEnabled: Boolean,
    val currentFacility: Facility? = null
) : Parcelable {

  companion object {
    fun create(
        patientUuid: UUID,
        appointmentConfig: AppointmentConfig,
        userClock: UserClock,
        mode: UiMode,
        secureCallFeatureEnabled: Boolean,
        overdueListChangesFeatureEnabled: Boolean
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
          overdueListChangesFeatureEnabled = overdueListChangesFeatureEnabled
      )
    }
  }

  val hasCurrentFacility: Boolean
    get() = currentFacility != null

  val hasLoadedPatientProfile: Boolean
    get() = patientProfile != null

  val patientProfileHasPhoneNumber: Boolean
    get() = !patientProfile?.phoneNumbers.isNullOrEmpty()

  val hasLoadedAppointment: Boolean
    get() = appointment != null

  val isAppointmentPresent : Boolean
  get() = appointment?.isPresent() == true

  val hasRegisteredFacility: Boolean
    get() = appointment?.isPresent() == true && appointment.get().patientRegisteredFacilityID != null

  val appointmentIsInRegisteredFacility: Boolean
    get() = appointment?.get()?.patientRegisteredFacilityID == currentFacility?.uuid

  val appointmentUuid: UUID
    get() = appointment!!.get().appointment.uuid

  val isPatientContactInfoLoaded: Boolean
    get() = contactPatientInfoProgressState == DONE

  fun contactPatientProfileLoaded(contactPatientProfile: ContactPatientProfile): ContactPatientModel {
    return copy(patientProfile = contactPatientProfile)
  }

  fun overdueAppointmentLoaded(appointment: Optional<OverdueAppointment>): ContactPatientModel {
    return copy(appointment = appointment.parcelable())
  }

  fun reminderDateSelected(date: PotentialAppointmentDate): ContactPatientModel {
    return copy(selectedAppointmentDate = date.scheduledFor)
  }

  fun changeUiModeTo(newMode: UiMode): ContactPatientModel {
    return copy(uiMode = newMode)
  }

  fun removeAppointmentReasonSelected(reason: RemoveAppointmentReason): ContactPatientModel {
    return copy(selectedRemoveAppointmentReason = reason)
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
}
