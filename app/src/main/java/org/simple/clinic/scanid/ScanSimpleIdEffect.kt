package org.simple.clinic.scanid

import org.simple.clinic.patient.businessid.Identifier

sealed class ScanSimpleIdEffect

object ShowQrCodeScannerView : ScanSimpleIdEffect()

object HideQrCodeScannerView : ScanSimpleIdEffect()

object HideShortCodeValidationError : ScanSimpleIdEffect()

data class ShowShortCodeValidationError(val failure: EnteredCodeValidationResult) : ScanSimpleIdEffect()

data class ValidateShortCode(val enteredCode: EnteredCodeInput) : ScanSimpleIdEffect()

data class SendScannedIdentifierResult(val scannedId: ScanResult) : ScanSimpleIdEffect()

data class SearchPatientByIdentifier(val identifier: Identifier) : ScanSimpleIdEffect()
