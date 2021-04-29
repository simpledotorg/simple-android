package org.simple.clinic.scanid

import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.widgets.UiEvent

sealed class ScanSimpleIdEvent : UiEvent

object ShowKeyboard : ScanSimpleIdEvent() {
  override val analyticsName: String
    get() = "Scan Simple Card:Show keyboard"
}

object HideKeyboard : ScanSimpleIdEvent() {
  override val analyticsName: String
    get() = "Scan Simple Card:Hide keyboard"
}

object ShortCodeChanged : ScanSimpleIdEvent() {
  override val analyticsName: String
    get() = "Scan Simple Card:Short code changed"
}

data class ShortCodeValidated(val result: ShortCodeValidationResult) : ScanSimpleIdEvent()

data class ShortCodeSearched(val shortCode: ShortCodeInput) : ScanSimpleIdEvent() {
  override val analyticsName: String
    get() = "Scan Simple Card:Short code searched"
}

data class ScanSimpleIdScreenQrCodeScanned(val text: String) : ScanSimpleIdEvent() {
  override val analyticsName = "Scan Simple Card:QR code scanned"
}

data class PatientSearchByIdentifierCompleted(
    val patients: List<Patient>,
    val identifier: Identifier
) : ScanSimpleIdEvent()

data class ScannedQRCodeJsonParsed(val patientPrefillInfo: PatientPrefillInfo?): ScanSimpleIdEvent()
