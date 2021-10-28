package org.simple.clinic.summary

import org.simple.clinic.facility.Facility
import org.simple.clinic.patient.businessid.Identifier
import java.time.Instant
import java.util.UUID

sealed class PatientSummaryEffect

data class LoadPatientSummaryProfile(val patientUuid: UUID) : PatientSummaryEffect()

object LoadCurrentUserAndFacility : PatientSummaryEffect()

data class CheckForInvalidPhone(val patientUuid: UUID) : PatientSummaryEffect()

data class MarkReminderAsShown(val patientUuid: UUID) : PatientSummaryEffect()

data class LoadDataForBackClick(
    val patientUuid: UUID,
    val screenCreatedTimestamp: Instant
) : PatientSummaryEffect()

data class LoadDataForDoneClick(val patientUuid: UUID) : PatientSummaryEffect()

data class TriggerSync(val sheetOpenedFrom: AppointmentSheetOpenedFrom) : PatientSummaryEffect()

data class FetchHasShownMissingPhoneReminder(val patientUuid: UUID) : PatientSummaryEffect()

object LoadMedicalOfficers : PatientSummaryEffect()

object ShowAddMeasurementsWarningDialog : PatientSummaryEffect()

object ShowAddBloodPressureWarningDialog : PatientSummaryEffect()

object ShowAddBloodSugarWarningDialog : PatientSummaryEffect()

object OpenSelectFacilitySheet : PatientSummaryEffect()

data class DispatchNewAssignedFacility(val facility: Facility) : PatientSummaryEffect()

sealed class PatientSummaryViewEffect : PatientSummaryEffect()

data class HandleEditClick(
    val patientSummaryProfile: PatientSummaryProfile,
    val currentFacility: Facility
) : PatientSummaryViewEffect()

object GoBackToPreviousScreen : PatientSummaryViewEffect()

object GoToHomeScreen : PatientSummaryViewEffect()

data class ShowAddPhonePopup(val patientUuid: UUID) : PatientSummaryViewEffect()

data class ShowLinkIdWithPatientView(
    val patientUuid: UUID,
    val identifier: Identifier
) : PatientSummaryViewEffect()

data class ShowScheduleAppointmentSheet(
    val patientUuid: UUID,
    val sheetOpenedFrom: AppointmentSheetOpenedFrom,
    val currentFacility: Facility
) : PatientSummaryViewEffect()

object ShowDiagnosisError : PatientSummaryViewEffect()

data class OpenContactPatientScreen(val patientUuid: UUID) : PatientSummaryViewEffect()

data class NavigateToTeleconsultRecordScreen(
    val patientUuid: UUID,
    val teleconsultRecordId: UUID
) : PatientSummaryViewEffect()

data class OpenContactDoctorSheet(val patientUuid: UUID) : PatientSummaryViewEffect()
