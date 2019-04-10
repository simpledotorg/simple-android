package org.simple.clinic.summary.linkId

import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

typealias Ui = LinkIdWithPatientSheet
typealias UiChange = (Ui) -> Unit

class LinkIdWithPatientSheetController @Inject constructor() : ObservableTransformer<UiEvent, UiChange> {
  override fun apply(events: Observable<UiEvent>): Observable<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .compose(ReportAnalyticsEvents())
        .replay()

    return Observable.never()
  }

}