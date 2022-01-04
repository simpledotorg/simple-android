package org.simple.clinic.scanid

import org.simple.clinic.patient.CompleteMedicalRecord
import org.simple.clinic.patient.PatientPrefillInfo
import org.simple.clinic.patient.businessid.Identifier
import java.util.UUID

sealed class ScanSimpleIdEffect

data class ValidateEnteredCode(val enteredCode: EnteredCodeInput) : ScanSimpleIdEffect()

data class SearchPatientByIdentifier(val identifier: Identifier) : ScanSimpleIdEffect()

data class ParseScannedJson(val text: String) : ScanSimpleIdEffect()

data class OnlinePatientLookupWithIdentifier(val identifier: Identifier) : ScanSimpleIdEffect()

data class SaveCompleteMedicalRecords(
    val completeMedicalRecords: List<CompleteMedicalRecord>
) : ScanSimpleIdEffect()

sealed class ScanSimpleIdViewEffect : ScanSimpleIdEffect()

object ShowQrCodeScannerView : ScanSimpleIdViewEffect()

object HideQrCodeScannerView : ScanSimpleIdViewEffect()

object HideEnteredCodeValidationError : ScanSimpleIdViewEffect()

data class ShowEnteredCodeValidationError(val failure: EnteredCodeValidationResult) : ScanSimpleIdViewEffect()

data class OpenPatientSummary(val patientId: UUID) : ScanSimpleIdViewEffect()

data class OpenPatientSearch(
    val additionalIdentifier: Identifier?,
    val initialSearchQuery: String?,
    val patientPrefillInfo: PatientPrefillInfo?
) : ScanSimpleIdViewEffect()

data class GoBackToEditPatientScreen(
    val identifier: Identifier
) : ScanSimpleIdViewEffect()

data class ShowScannedQrCodeError(val scanErrorState: ScanErrorState) : ScanSimpleIdViewEffect()
