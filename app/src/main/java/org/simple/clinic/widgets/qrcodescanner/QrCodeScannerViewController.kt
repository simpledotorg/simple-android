package org.simple.clinic.widgets.qrcodescanner

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.TheActivityLifecycle
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

typealias Ui = QrCodeScannerView
typealias UiChange = (Ui) -> Unit

class QrCodeScannerViewController @Inject constructor() : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .compose(ReportAnalyticsEvents())
        .replay()

    return Observable.merge(
        startScanningForQrCode(replayedEvents),
        stopScanningForQrCode(replayedEvents)
    )
  }

  private fun startScanningForQrCode(events: Observable<UiEvent>): Observable<UiChange> {
    val screenCreates = events.ofType<ScreenCreated>()
    val activityResumes = events.ofType<TheActivityLifecycle.Resumed>()

    return Observable
        .merge(screenCreates, activityResumes)
        .map { Ui::startScanning }
  }

  private fun stopScanningForQrCode(events: Observable<UiEvent>): Observable<UiChange> {
    return Observable
        .merge(
            events.ofType<ScreenDestroyed>(),
            events.ofType<TheActivityLifecycle.Paused>()
        )
        .map { Ui::stopScanning }
  }
}
