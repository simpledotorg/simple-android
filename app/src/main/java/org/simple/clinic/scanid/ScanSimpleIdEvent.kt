package org.simple.clinic.scanid

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
