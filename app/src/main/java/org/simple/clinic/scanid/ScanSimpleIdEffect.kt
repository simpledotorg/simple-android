package org.simple.clinic.scanid

import org.simple.clinic.patient.businessid.Identifier

sealed class ScanSimpleIdEffect

object ShowQrCodeScannerView : ScanSimpleIdEffect()

object HideQrCodeScannerView : ScanSimpleIdEffect()

object HideShortCodeValidationError : ScanSimpleIdEffect()

data class ShowShortCodeValidationError(val failure: ShortCodeValidationResult) : ScanSimpleIdEffect()

data class ValidateShortCode(val shortCode: ShortCodeInput) : ScanSimpleIdEffect()

data class SendScannedIdentifierResult(val scannedId: Identifier): ScanSimpleIdEffect()
