package org.simple.clinic.home.bp

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

typealias Ui = NewBpScreen
typealias UiChange = (Ui) -> Unit

class NewBpScreenController @Inject constructor() : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = events.replay().refCount()

    return Observable.mergeArray(
            newPatientClicks(replayedEvents),
            aadhaarScanClicks(replayedEvents))
  }

  private fun newPatientClicks(events: Observable<UiEvent>): ObservableSource<UiChange> {
    return events.ofType(NewPatientClicked::class.java)
        .map { { ui: NewBpScreen -> ui.openNewPatientScreen() } }
  }

  private fun aadhaarScanClicks(events: Observable<UiEvent>): ObservableSource<UiChange> {
    return events.ofType(ScanAadhaarClicked::class.java)
            .map { { ui: NewBpScreen -> ui.openAadhaarScanScreen() } }
  }
}
