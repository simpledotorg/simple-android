package org.simple.clinic.scanid

import org.simple.clinic.patient.businessid.Identifier
import java.util.UUID

sealed class ScanSimpleIdEffect

object ShowQrCodeScannerView : ScanSimpleIdEffect()

object HideQrCodeScannerView : ScanSimpleIdEffect()

object HideEnteredCodeValidationError : ScanSimpleIdEffect()

data class ShowEnteredCodeValidationError(val failure: EnteredCodeValidationResult) : ScanSimpleIdEffect()

data class ValidateEnteredCode(val enteredCode: EnteredCodeInput) : ScanSimpleIdEffect()

data class SendScannedIdentifierResult(val scannedId: ScanResult) : ScanSimpleIdEffect()

data class SearchPatientByIdentifier(val identifier: Identifier) : ScanSimpleIdEffect()

data class ParseScannedJson(val text: String) : ScanSimpleIdEffect()

data class OpenPatientSummary(val patientId: UUID) : ScanSimpleIdEffect()

data class OpenShortCodeSearch(val shortCode: String) : ScanSimpleIdEffect()
