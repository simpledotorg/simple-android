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

object EnteredCodeChanged : ScanSimpleIdEvent() {
  override val analyticsName: String
    get() = "Scan Simple Card:Entered code changed"
}

data class EnteredCodeValidated(val result: EnteredCodeValidationResult) : ScanSimpleIdEvent()

data class EnteredCodeSearched(val enteredCode: EnteredCodeInput) : ScanSimpleIdEvent() {
  override val analyticsName: String
    get() = "Scan Simple Card:Entered code searched"
}

data class ScanSimpleIdScreenQrCodeScanned(val text: String) : ScanSimpleIdEvent() {
  override val analyticsName = "Scan Simple Card:QR code scanned"
}

data class PatientSearchByIdentifierCompleted(
    val patients: List<Patient>,
    val identifier: Identifier
) : ScanSimpleIdEvent()

data class ScannedQRCodeJsonParsed(
    val patientPrefillInfo: PatientPrefillInfo?,
    val healthIdNumber: String?
) : ScanSimpleIdEvent()

object InvalidQrCode : ScanSimpleIdEvent()
