package org.simple.clinic.summary

import org.simple.clinic.facility.Facility
import org.simple.clinic.medicalhistory.MedicalHistory
import org.simple.clinic.summary.teleconsultation.sync.MedicalOfficer
import org.simple.clinic.user.User
import org.simple.clinic.widgets.UiEvent
import java.time.Instant
import java.util.UUID

sealed class PatientSummaryEvent : UiEvent

data class PatientSummaryProfileLoaded(val patientSummaryProfile: PatientSummaryProfile) : PatientSummaryEvent()

// TODO(vs): 2020-01-15 Consider whether these should be moved to the effect handler as properties later
data class PatientSummaryBackClicked(
    val patientUuid: UUID,
    val screenCreatedTimestamp: Instant
) : PatientSummaryEvent() {
  override val analyticsName = "Patient Summary:Back Clicked"
}

// TODO(vs): 2020-01-16 Consider whether these should be moved to the effect handler as properties later
data class PatientSummaryDoneClicked(val patientUuid: UUID) : PatientSummaryEvent() {
  override val analyticsName = "Patient Summary:Done Clicked"
}

data class CurrentUserAndFacilityLoaded(
    val user: User,
    val facility: Facility
) : PatientSummaryEvent()

object PatientSummaryEditClicked : PatientSummaryEvent()

data class ScheduledAppointment(val sheetOpenedFrom: AppointmentSheetOpenedFrom) : PatientSummaryEvent() {
  override val analyticsName = "Patient Summary:Schedule Appointment Sheet Closed"
}

object CompletedCheckForInvalidPhone : PatientSummaryEvent()

object PatientSummaryBloodPressureSaved : PatientSummaryEvent()

data class DataForBackClickLoaded(
    val hasPatientDataChangedSinceScreenCreated: Boolean,
    val countOfRecordedBloodPressures: Int,
    val countOfRecordedBloodSugars: Int,
    val medicalHistory: MedicalHistory
) : PatientSummaryEvent()

data class DataForDoneClickLoaded(
    val countOfRecordedBloodPressures: Int,
    val countOfRecordedBloodSugars: Int,
    val medicalHistory: MedicalHistory
) : PatientSummaryEvent()

data class SyncTriggered(val sheetOpenedFrom: AppointmentSheetOpenedFrom) : PatientSummaryEvent()

data class FetchedHasShownMissingPhoneReminder(val hasShownReminder: Boolean) : PatientSummaryEvent()

object ContactPatientClicked : PatientSummaryEvent() {
  override val analyticsName: String = "Patient Summary:Phone Number Clicked"
}

object ContactDoctorClicked : PatientSummaryEvent() {
  override val analyticsName: String = "Patient Summary:Contact Doctor Clicked"
}

object LogTeleconsultClicked : PatientSummaryEvent()

data class MedicalOfficersLoaded(val medicalOfficers: List<MedicalOfficer>) : PatientSummaryEvent()

object ChangeAssignedFacilityClicked : PatientSummaryEvent() {
  override val analyticsName: String = "Assigned Facility:Change Facility"
}

data class NewAssignedFacilitySelected(val facility: Facility) : PatientSummaryEvent() {
  override val analyticsName: String = "Assigned Facility:Facility Selected"
}
