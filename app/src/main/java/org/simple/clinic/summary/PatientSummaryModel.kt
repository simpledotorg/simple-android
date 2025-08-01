package org.simple.clinic.summary

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.cvdrisk.StatinInfo
import org.simple.clinic.facility.Facility
import org.simple.clinic.overdue.Appointment
import org.simple.clinic.patient.PatientStatus
import org.simple.clinic.summary.teleconsultation.sync.MedicalOfficer
import org.simple.clinic.user.User
import org.simple.clinic.util.ParcelableOptional
import org.simple.clinic.util.parcelable
import org.simple.clinic.util.toOptional
import java.util.UUID

@Parcelize
data class PatientSummaryModel(
    val openIntention: OpenIntention,
    val patientUuid: UUID,
    val patientSummaryProfile: PatientSummaryProfile?,
    val currentFacility: Facility?,
    val hasCheckedForInvalidPhone: Boolean,
    val userLoggedInStatus: User.LoggedInStatus?,
    val medicalOfficers: List<MedicalOfficer>?,
    val hasShownMeasurementsWarningDialog: Boolean,
    val hasPatientRegistrationData: Boolean?,
    val isNewestBpEntryHigh: Boolean?,
    val hasPrescribedDrugsChangedToday: Boolean?,
    val scheduledAppointment: ParcelableOptional<Appointment>?,
    val hasShownDiagnosisWarningDialog: Boolean,
    val statinInfo: StatinInfo?,
    val hasShownTobaccoUseDialog: Boolean,
) : Parcelable, PatientSummaryChildModel {

  companion object {
    fun from(openIntention: OpenIntention, patientUuid: UUID): PatientSummaryModel {
      return PatientSummaryModel(
          openIntention = openIntention,
          patientUuid = patientUuid,
          patientSummaryProfile = null,
          currentFacility = null,
          hasCheckedForInvalidPhone = false,
          userLoggedInStatus = null,
          medicalOfficers = null,
          hasShownMeasurementsWarningDialog = false,
          hasPatientRegistrationData = null,
          isNewestBpEntryHigh = null,
          hasPrescribedDrugsChangedToday = null,
          scheduledAppointment = null,
          hasShownDiagnosisWarningDialog = false,
          statinInfo = null,
          hasShownTobaccoUseDialog = false,
      )
    }
  }

  val hasPatientDied: Boolean
    get() = patientSummaryProfile!!.patient.status == PatientStatus.Dead

  val hasLoadedPatientSummaryProfile: Boolean
    get() = patientSummaryProfile != null

  val hasLoadedCurrentFacility: Boolean
    get() = currentFacility != null

  val isDiabetesManagementEnabled: Boolean
    get() = currentFacility!!.config.diabetesManagementEnabled

  val isTeleconsultationEnabled: Boolean
    get() = currentFacility!!.config.teleconsultationEnabled == true

  val isUserLoggedIn: Boolean
    get() = userLoggedInStatus == User.LoggedInStatus.LOGGED_IN

  val hasUserLoggedInStatus: Boolean
    get() = userLoggedInStatus != null

  val hasAssignedFacility: Boolean
    get() = patientSummaryProfile?.patient?.assignedFacilityId != null

  val hasMedicalOfficers: Boolean
    get() = medicalOfficers.isNullOrEmpty().not()

  val hasScheduledAppointment: Boolean
    get() = scheduledAppointment != null && scheduledAppointment.isPresent()

  val hasStatinInfoLoaded: Boolean
    get() = statinInfo != null

  override fun readyToRender(): Boolean {
    return hasLoadedPatientSummaryProfile && hasLoadedCurrentFacility && hasPatientRegistrationData != null
  }

  fun patientSummaryProfileLoaded(patientSummaryProfile: PatientSummaryProfile): PatientSummaryModel {
    return copy(patientSummaryProfile = patientSummaryProfile)
  }

  fun currentFacilityLoaded(facility: Facility): PatientSummaryModel {
    return copy(currentFacility = facility)
  }

  fun completedCheckForInvalidPhone(): PatientSummaryModel {
    return copy(hasCheckedForInvalidPhone = true)
  }

  fun userLoggedInStatusLoaded(loggedInStatus: User.LoggedInStatus?): PatientSummaryModel {
    return copy(userLoggedInStatus = loggedInStatus)
  }

  fun medicalOfficersLoaded(medicalOfficers: List<MedicalOfficer>): PatientSummaryModel {
    return copy(medicalOfficers = medicalOfficers)
  }

  fun shownMeasurementsWarningDialog(): PatientSummaryModel {
    return copy(hasShownMeasurementsWarningDialog = true)
  }

  fun shownDiagnosisWarningDialog(): PatientSummaryModel {
    return copy(hasShownDiagnosisWarningDialog = true)
  }

  fun patientRegistrationDataLoaded(hasPatientRegistrationData: Boolean): PatientSummaryModel {
    return copy(hasPatientRegistrationData = hasPatientRegistrationData)
  }

  fun clinicalDecisionSupportInfoLoaded(isNewestBpEntryHigh: Boolean, hasPrescribedDrugsChangedToday: Boolean): PatientSummaryModel {
    return copy(isNewestBpEntryHigh = isNewestBpEntryHigh, hasPrescribedDrugsChangedToday = hasPrescribedDrugsChangedToday)
  }

  fun scheduledAppointmentLoaded(appointment: Appointment?): PatientSummaryModel {
    return copy(scheduledAppointment = appointment.toOptional().parcelable())
  }

  fun updateStatinInfo(statinInfo: StatinInfo): PatientSummaryModel {
    return copy(statinInfo = statinInfo)
  }

  fun showTobaccoUseDialog(): PatientSummaryModel {
    return copy(hasShownTobaccoUseDialog = true)
  }
}
