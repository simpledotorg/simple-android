package org.simple.clinic.scanid

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.widgets.UiEvent
import java.util.UUID
import javax.inject.Inject

typealias Ui = ScanSimpleIdScreen
typealias UiChange = (Ui) -> Unit

class ScanSimpleIdScreenController @Inject constructor() : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .compose(mergeWithScannedBpPassportCodes())
        .compose(ReportAnalyticsEvents())
        .replay()

    return sendScannedBpPassportCodes(replayedEvents)
  }

  private fun sendScannedBpPassportCodes(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<ScanSimpleIdScreenPassportCodeScanned.ValidPassportCode>()
        .take(1)
        .map { { ui: Ui -> ui.sendScannedPassportCode(it.bpPassportUuid) } }
  }

  private fun mergeWithScannedBpPassportCodes() = ObservableTransformer<UiEvent, UiEvent> { events ->

    val scannedBpPassportCodes = events
        .ofType<ScanSimpleIdScreenQrCodeScanned>()
        .map { it.text }
        .map { scannedQrCode ->
          try {
            val bpPassportCode = UUID.fromString(scannedQrCode)
            ScanSimpleIdScreenPassportCodeScanned.ValidPassportCode(bpPassportCode)
          } catch (e: IllegalArgumentException) {
            ScanSimpleIdScreenPassportCodeScanned.InvalidPassportCode
          }
        }

    events.mergeWith(scannedBpPassportCodes)
  }
}
