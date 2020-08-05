package org.simple.clinic.scanid

sealed class ScanSimpleIdEffect

object ShowQrCodeScannerView : ScanSimpleIdEffect()

object HideQrCodeScannerView : ScanSimpleIdEffect()

object HideShortCodeValidationError : ScanSimpleIdEffect()

data class ShowShortCodeValidationError(val failure: ShortCodeValidationResult) : ScanSimpleIdEffect()

data class ValidateShortCode(val shortCode: ShortCodeInput) : ScanSimpleIdEffect()
