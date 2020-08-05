package org.simple.clinic.scanid

import java.util.UUID

sealed class ScanSimpleIdEffect

object ShowQrCodeScannerView : ScanSimpleIdEffect()

object HideQrCodeScannerView : ScanSimpleIdEffect()

object HideShortCodeValidationError : ScanSimpleIdEffect()

data class ShowShortCodeValidationError(val failure: ShortCodeValidationResult) : ScanSimpleIdEffect()

data class ValidateShortCode(val shortCode: ShortCodeInput) : ScanSimpleIdEffect()

data class OpenPatientShortCodeSearch(val shortCode: String) : ScanSimpleIdEffect()

data class OpenPatientSummary(val patientUuid: UUID) : ScanSimpleIdEffect()
