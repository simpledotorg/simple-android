package org.simple.clinic.summary.updatephone

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.widgets.UiEvent

typealias Ui = UpdatePhoneNumberDialog
typealias UiChange = (Ui) -> Unit

class UpdatePhoneNumberDialogController : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = events.compose(ReportAnalyticsEvents())
        .replay(1)
        .refCount()

    return Observable.empty()
  }
}
