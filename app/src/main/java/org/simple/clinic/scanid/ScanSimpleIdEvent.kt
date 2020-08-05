package org.simple.clinic.scanid

import org.simple.clinic.patient.Patient
import org.simple.clinic.util.Optional
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

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

data class PatientSearchCompleted(val patient: Optional<Patient>, val scannedId: UUID) : ScanSimpleIdEvent()
