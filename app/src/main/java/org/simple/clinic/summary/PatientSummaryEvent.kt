package org.simple.clinic.summary

import org.simple.clinic.cvdrisk.CVDRisk
import org.simple.clinic.cvdrisk.CVDRiskRange
import org.simple.clinic.drugs.DiagnosisWarningPrescriptions
import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.facility.Facility
import org.simple.clinic.medicalhistory.Answer
import org.simple.clinic.medicalhistory.MedicalHistory
import org.simple.clinic.overdue.Appointment
import org.simple.clinic.patientattribute.BMIReading
import org.simple.clinic.patientattribute.PatientAttribute
import org.simple.clinic.reassignpatient.ReassignPatientSheetClosedFrom
import org.simple.clinic.reassignpatient.ReassignPatientSheetOpenedFrom
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
data class PatientSummaryDoneClicked(
    val patientUuid: UUID,
    val screenCreatedTimestamp: Instant
) : PatientSummaryEvent() {
  override val analyticsName = "Patient Summary:Done Clicked"
}

data class MeasurementWarningNotNowClicked(
    val patientUuid: UUID,
    val screenCreatedTimestamp: Instant
) : PatientSummaryEvent() {
  override val analyticsName = "Patient Summary:Measurement Warning Not Now Clicked"
}

data class CurrentUserAndFacilityLoaded(
    val user: User,
    val facility: Facility
) : PatientSummaryEvent()

data object PatientSummaryEditClicked : PatientSummaryEvent()

data class ScheduledAppointment(val sheetOpenedFrom: AppointmentSheetOpenedFrom) : PatientSummaryEvent() {
  override val analyticsName = "Patient Summary:Schedule Appointment Sheet Closed"
}

data class CompletedCheckForInvalidPhone(val isPhoneInvalid: Boolean) : PatientSummaryEvent()

data object PatientSummaryBloodPressureSaved : PatientSummaryEvent()

data class DataForBackClickLoaded(
    val hasPatientMeasurementDataChangedSinceScreenCreated: Boolean,
    val hasAppointmentChangeSinceScreenCreated: Boolean,
    val countOfRecordedBloodPressures: Int,
    val countOfRecordedBloodSugars: Int,
    val medicalHistory: MedicalHistory,
    val canShowPatientReassignmentWarning: Boolean,
    val prescribedDrugs: List<PrescribedDrug>,
    val diagnosisWarningPrescriptions: DiagnosisWarningPrescriptions
) : PatientSummaryEvent()

data class DataForDoneClickLoaded(
    val hasPatientMeasurementDataChangedSinceScreenCreated: Boolean,
    val hasAppointmentChangeSinceScreenCreated: Boolean,
    val countOfRecordedBloodPressures: Int,
    val countOfRecordedBloodSugars: Int,
    val medicalHistory: MedicalHistory,
    val canShowPatientReassignmentWarning: Boolean,
    val prescribedDrugs: List<PrescribedDrug>,
    val diagnosisWarningPrescriptions: DiagnosisWarningPrescriptions,
) : PatientSummaryEvent()

data class SyncTriggered(val sheetOpenedFrom: AppointmentSheetOpenedFrom) : PatientSummaryEvent()

data class FetchedHasShownMissingPhoneReminder(val hasShownReminder: Boolean) : PatientSummaryEvent()

data object ContactPatientClicked : PatientSummaryEvent() {
  override val analyticsName: String = "Patient Summary:Phone Number Clicked"
}

data object ContactDoctorClicked : PatientSummaryEvent() {
  override val analyticsName: String = "Patient Summary:Contact Doctor Clicked"
}

data object LogTeleconsultClicked : PatientSummaryEvent()

data class MedicalOfficersLoaded(val medicalOfficers: List<MedicalOfficer>) : PatientSummaryEvent()

data object ChangeAssignedFacilityClicked : PatientSummaryEvent() {
  override val analyticsName: String = "Assigned Facility:Change Facility"
}

data class NewAssignedFacilitySelected(val facility: Facility) : PatientSummaryEvent() {
  override val analyticsName: String = "Assigned Facility:Facility Selected"
}

data class PatientRegistrationDataLoaded(
    val countOfPrescribedDrugs: Int,
    val countOfRecordedBloodPressures: Int,
    val countOfRecordedBloodSugars: Int
) : PatientSummaryEvent()

data object NextAppointmentActionClicked : PatientSummaryEvent() {
  override val analyticsName: String = "Next Appointment Card:Action Button Clicked"
}

data object AssignedFacilityChanged : PatientSummaryEvent()

data class ClinicalDecisionSupportInfoLoaded(val isNewestBpEntryHigh: Boolean, val hasPrescribedDrugsChangedToday: Boolean) : PatientSummaryEvent()

data class CDSSPilotStatusChecked(val isPilotEnabledForFacility: Boolean) : PatientSummaryEvent()

data class LatestScheduledAppointmentLoaded(val appointment: Appointment?) : PatientSummaryEvent()

data class PatientReassignmentStatusLoaded(
    val isPatientEligibleForReassignment: Boolean,
    val clickAction: ClickAction,
    val screenCreatedTimestamp: Instant,
) : PatientSummaryEvent()

data class PatientReassignmentWarningClosed(
    val patientUuid: UUID,
    val screenCreatedTimestamp: Instant,
    val sheetOpenedFrom: ReassignPatientSheetOpenedFrom,
    val sheetClosedFrom: ReassignPatientSheetClosedFrom,
) : PatientSummaryEvent()

data object HasDiabetesClicked : PatientSummaryEvent()

data class HasHypertensionClicked(val continueToDiabetesDiagnosisWarning: Boolean) : PatientSummaryEvent()

data class HypertensionNotNowClicked(val continueToDiabetesDiagnosisWarning: Boolean) : PatientSummaryEvent()

data class StatinPrescriptionCheckInfoLoaded(
    val age: Int,
    val isPatientDead: Boolean,
    val wasBPMeasuredWithin90Days: Boolean,
    val medicalHistory: MedicalHistory,
    val patientAttribute: PatientAttribute?,
    val prescriptions: List<PrescribedDrug>,
    val cvdRiskRange: CVDRiskRange?,
    val hasMedicalHistoryChanged: Boolean,
    val wasCVDCalculatedWithin90Days: Boolean,
) : PatientSummaryEvent()

data class CVDRiskCalculated(
    val oldRisk: CVDRisk?,
    val newRiskRange: CVDRiskRange?
) : PatientSummaryEvent()

data object CVDRiskUpdated : PatientSummaryEvent()

data class StatinInfoLoaded(
    val age: Int,
    val medicalHistory: MedicalHistory,
    val canPrescribeStatin: Boolean,
    val riskRange: CVDRiskRange?,
    val bmiReading: BMIReading?,
) : PatientSummaryEvent()

data object AddTobaccoUseClicked : PatientSummaryEvent()

data class TobaccoUseAnswered(
    val isSmoker: Answer,
    val isUsingSmokelessTobacco: Answer = Answer.Unanswered
) : PatientSummaryEvent()

data object BMIReadingAdded : PatientSummaryEvent()

data object AddBMIClicked : PatientSummaryEvent()

data object AddCholesterolClicked: PatientSummaryEvent()

data object CholesterolAdded : PatientSummaryEvent()
