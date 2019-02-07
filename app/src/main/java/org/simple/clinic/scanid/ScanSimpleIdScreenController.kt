package org.simple.clinic.scanid

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

typealias Ui = ScanSimpleIdScreen
typealias UiChange = (Ui) -> Unit

class ScanSimpleIdScreenController @Inject constructor() : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .compose(ReportAnalyticsEvents())
        .replay()

    return replayedEvents
        .ofType<ScanSimpleIdScreenQrCodeScanned>()
        .map { it.text }
        .map { text -> { ui: Ui -> ui.showPatientSearchResults(text) } }
  }
}
