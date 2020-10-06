package org.simple.clinic.summary

import org.simple.clinic.facility.Facility
import org.simple.clinic.patient.businessid.BusinessId
import org.simple.clinic.patient.businessid.Identifier
import java.time.Instant
import java.util.UUID

sealed class PatientSummaryEffect

data class LoadPatientSummaryProfile(val patientUuid: UUID) : PatientSummaryEffect()

object LoadCurrentUserAndFacility : PatientSummaryEffect()

data class HandleEditClick(
    val patientSummaryProfile: PatientSummaryProfile,
    val currentFacility: Facility
) : PatientSummaryEffect()

object HandleLinkIdCancelled: PatientSummaryEffect()

object GoBackToPreviousScreen: PatientSummaryEffect()

object GoToHomeScreen: PatientSummaryEffect()

data class CheckForInvalidPhone(val patientUuid: UUID): PatientSummaryEffect()

data class MarkReminderAsShown(val patientUuid: UUID): PatientSummaryEffect()

data class ShowAddPhonePopup(val patientUuid: UUID): PatientSummaryEffect()

data class ShowLinkIdWithPatientView(val patientUuid: UUID, val identifier: Identifier): PatientSummaryEffect()

object HideLinkIdWithPatientView : PatientSummaryEffect()

data class ShowScheduleAppointmentSheet(
    val patientUuid: UUID,
    val sheetOpenedFrom: AppointmentSheetOpenedFrom,
    val currentFacility: Facility
): PatientSummaryEffect()

data class LoadDataForBackClick(
    val patientUuid: UUID,
    val screenCreatedTimestamp: Instant
) : PatientSummaryEffect()

data class LoadDataForDoneClick(val patientUuid: UUID): PatientSummaryEffect()

data class TriggerSync(val sheetOpenedFrom: AppointmentSheetOpenedFrom): PatientSummaryEffect()

object ShowDiagnosisError : PatientSummaryEffect()

data class FetchHasShownMissingPhoneReminder(val patientUuid: UUID): PatientSummaryEffect()

data class OpenContactPatientScreen(val patientUuid: UUID): PatientSummaryEffect()

data class NavigateToTeleconsultRecordScreen(
    val patientUuid: UUID,
    val teleconsultRecordId: UUID
) : PatientSummaryEffect()

object LoadMedicalOfficers : PatientSummaryEffect()

data class OpenContactDoctorSheet(val patientUuid: UUID) : PatientSummaryEffect()
