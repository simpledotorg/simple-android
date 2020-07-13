package org.simple.clinic.home.report

import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.reports.ReportsRepository
import org.simple.clinic.util.extractIfPresent
import org.simple.clinic.util.filterNotPresent
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

typealias Ui = ReportsUi
typealias UiChange = (Ui) -> Unit

class ReportsScreenController @Inject constructor(
    private val reportsRepository: ReportsRepository
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): Observable<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .replay()

    return Observable.never()
  }
}
