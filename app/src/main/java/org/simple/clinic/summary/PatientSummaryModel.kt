package org.simple.clinic.summary

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.facility.Facility
import org.simple.clinic.patient.PatientStatus
import org.simple.clinic.summary.teleconsultation.sync.MedicalOfficer
import org.simple.clinic.user.User
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
    val hasPatientRegistrationData: Boolean?
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
          hasPatientRegistrationData = null
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

  fun patientRegistrationDataLoaded(hasPatientRegistrationData: Boolean): PatientSummaryModel {
    return copy(hasPatientRegistrationData = hasPatientRegistrationData)
  }
}
