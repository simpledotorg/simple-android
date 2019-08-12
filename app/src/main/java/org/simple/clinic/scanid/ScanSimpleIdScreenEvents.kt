package org.simple.clinic.scanid

import org.simple.clinic.widgets.UiEvent
import java.util.UUID

sealed class ScanSimpleIdScreenEvent : UiEvent

data class ScanSimpleIdScreenQrCodeScanned(val text: String) : ScanSimpleIdScreenEvent() {
  override val analyticsName = "Scan Simple Card:QR code scanned"
}

sealed class ScanSimpleIdScreenPassportCodeScanned : ScanSimpleIdScreenEvent() {

  data class ValidPassportCode(val bpPassportUuid: UUID) : ScanSimpleIdScreenPassportCodeScanned() {
    override val analyticsName = "Scan Simple Card:Valid BP passport code scanned"
  }

  object InvalidPassportCode : ScanSimpleIdScreenPassportCodeScanned() {
    override val analyticsName = "Scan Simple Card:Invalid BP passport code scanned"
  }
}

data class ShortCodeSearched(val shortCode: ShortCodeInput) : ScanSimpleIdScreenEvent()

object ShowKeyboardEvent : ScanSimpleIdScreenEvent() // TODO Rename, drop the Event suffix

object HideKeyboardEvent : ScanSimpleIdScreenEvent()

object ShortCodeChanged : ScanSimpleIdScreenEvent()
