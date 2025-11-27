package org.simple.clinic.summary

import org.simple.clinic.cvdrisk.CVDRisk
import org.simple.clinic.cvdrisk.CVDRiskRange
import org.simple.clinic.facility.Facility
import org.simple.clinic.patient.Answer
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.reassignpatient.ReassignPatientSheetOpenedFrom
import java.time.Instant
import java.util.UUID
import org.simple.clinic.medicalhistory.Answer as MedicalHistoryAnswer

sealed class PatientSummaryEffect

data class LoadPatientSummaryProfile(val patientUuid: UUID) : PatientSummaryEffect()

data object LoadCurrentUserAndFacility : PatientSummaryEffect()

data class CheckForInvalidPhone(val patientUuid: UUID) : PatientSummaryEffect()

data class MarkReminderAsShown(val patientUuid: UUID) : PatientSummaryEffect()

data class LoadDataForBackClick(
    val patientUuid: UUID,
    val screenCreatedTimestamp: Instant,
    val canShowPatientReassignmentWarning: Boolean
) : PatientSummaryEffect()

data class LoadDataForDoneClick(
    val patientUuid: UUID,
    val screenCreatedTimestamp: Instant,
    val canShowPatientReassignmentWarning: Boolean
) : PatientSummaryEffect()

data class TriggerSync(val sheetOpenedFrom: AppointmentSheetOpenedFrom) : PatientSummaryEffect()

data class FetchHasShownMissingPhoneReminder(val patientUuid: UUID) : PatientSummaryEffect()

data object LoadMedicalOfficers : PatientSummaryEffect()

data class LoadPatientRegistrationData(val patientUuid: UUID) : PatientSummaryEffect()

data class LoadClinicalDecisionSupportInfo(val patientUuid: UUID) : PatientSummaryEffect()

data object CheckIfCDSSPilotIsEnabled : PatientSummaryEffect()

data class LoadLatestScheduledAppointment(val patientUuid: UUID) : PatientSummaryEffect()

data class UpdatePatientReassignmentStatus(val patientUuid: UUID, val status: Answer) : PatientSummaryEffect()

data class CheckPatientReassignmentStatus(
    val patientUuid: UUID,
    val clickAction: ClickAction,
    val screenCreatedTimestamp: Instant,
) : PatientSummaryEffect()

data class MarkDiabetesDiagnosis(val patientUuid: UUID) : PatientSummaryEffect()

data class MarkHypertensionDiagnosis(val patientUuid: UUID) : PatientSummaryEffect()

data class LoadStatinPrescriptionCheckInfo(val patient: Patient) : PatientSummaryEffect()

data class CalculateNonLabBasedCVDRisk(val patient: Patient) : PatientSummaryEffect()

data class CalculateLabBasedCVDRisk(val patient: Patient) : PatientSummaryEffect()

data class SaveCVDRisk(
    val patientUuid: UUID,
    val cvdRiskRange: CVDRiskRange
) : PatientSummaryEffect()

data class UpdateCVDRisk(
    val oldRisk: CVDRisk,
    val newRiskRange: CVDRiskRange
) : PatientSummaryEffect()

data class LoadStatinInfo(val patientUuid: UUID) : PatientSummaryEffect()

data class UpdateTobaccoUse(
    val patientId: UUID,
    val isSmoker: MedicalHistoryAnswer,
    val isUsingSmokelessTobacco: MedicalHistoryAnswer
) : PatientSummaryEffect()

sealed class PatientSummaryViewEffect : PatientSummaryEffect()

data class HandleEditClick(
    val patientSummaryProfile: PatientSummaryProfile,
    val currentFacility: Facility
) : PatientSummaryViewEffect()

data object GoBackToPreviousScreen : PatientSummaryViewEffect()

data object GoToHomeScreen : PatientSummaryViewEffect()

data class ShowAddPhonePopup(val patientUuid: UUID) : PatientSummaryViewEffect()

data class ShowUpdatePhonePopup(val patientUuid: UUID) : PatientSummaryViewEffect()

data class ShowLinkIdWithPatientView(
    val patientUuid: UUID,
    val identifier: Identifier
) : PatientSummaryViewEffect()

data class ShowScheduleAppointmentSheet(
    val patientUuid: UUID,
    val sheetOpenedFrom: AppointmentSheetOpenedFrom,
    val currentFacility: Facility
) : PatientSummaryViewEffect()

data object ShowDiagnosisRequiredError: PatientSummaryViewEffect()

data object ShowDiagnosisOrReferralRequiredError: PatientSummaryViewEffect()

data object ShowHypertensionDiagnosisRequiredError : PatientSummaryViewEffect()

data class OpenContactPatientScreen(val patientUuid: UUID) : PatientSummaryViewEffect()

data class NavigateToTeleconsultRecordScreen(
    val patientUuid: UUID,
    val teleconsultRecordId: UUID
) : PatientSummaryViewEffect()

data class OpenContactDoctorSheet(val patientUuid: UUID) : PatientSummaryViewEffect()

data object ShowAddMeasurementsWarningDialog : PatientSummaryViewEffect()

data object ShowAddBloodPressureWarningDialog : PatientSummaryViewEffect()

data object ShowAddBloodSugarWarningDialog : PatientSummaryViewEffect()

data object OpenSelectFacilitySheet : PatientSummaryViewEffect()

data class DispatchNewAssignedFacility(val facility: Facility) : PatientSummaryViewEffect()

data object RefreshNextAppointment : PatientSummaryViewEffect()

data class ShowReassignPatientWarningSheet(
    val patientUuid: UUID,
    val currentFacility: Facility,
    val sheetOpenedFrom: ReassignPatientSheetOpenedFrom,
) : PatientSummaryViewEffect()

data object ShowDiabetesDiagnosisWarning : PatientSummaryViewEffect()

data class ShowHypertensionDiagnosisWarning(val continueToDiabetesDiagnosisWarning: Boolean) : PatientSummaryViewEffect()

data object ShowTobaccoStatusDialog : PatientSummaryViewEffect()

data class OpenBMIEntrySheet(val patientUuid: UUID) : PatientSummaryViewEffect()

data class OpenCholesterolEntrySheet(val patientUuid: UUID) : PatientSummaryViewEffect()
