package org.simple.clinic.scheduleappointment

import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

typealias Ui = ScheduleAppointmentSheet
typealias UiChange = (Ui) -> Unit

class ScheduleAppointmentSheetController @Inject constructor(
    private val repository: AppointmentRepository
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(upstream: Observable<UiEvent>): Observable<UiChange> {
    val replayedEvents = upstream.compose(ReportAnalyticsEvents()).replay().refCount()

    return Observable.merge(
        sheetCreates(replayedEvents),
        dateIncrements(replayedEvents),
        dateDecrements(replayedEvents),
        schedulingSkips(replayedEvents))
  }

  private fun sheetCreates(events: Observable<UiEvent>): Observable<UiChange> {
    return events.ofType<SheetCreated>()
        .map { { ui: Ui -> ui.updateDisplayedDate(it.initialState) } }
  }

  private fun dateIncrements(events: Observable<UiEvent>): Observable<UiChange> {
    val notLastPositionStream = events.ofType<IncrementAppointmentDate>()
        .filter { (current, size) -> current != size - 1 }

    val updateDateStream = notLastPositionStream
        .map { { ui: Ui -> ui.updateDisplayedDate(it.currentState.inc()) } }

    val enableButtonStream = notLastPositionStream
        .map { { ui: Ui -> ui.enableIncrementButton(true) } }

    val lastPositionStream = events.ofType<IncrementAppointmentDate>()
        .filter { (current, size) -> current == size - 1 }
        .map { { ui: Ui -> ui.enableIncrementButton(false) } }

    return Observable.merge(updateDateStream, enableButtonStream, lastPositionStream)
  }

  private fun dateDecrements(events: Observable<UiEvent>): Observable<UiChange> {
    val notFirstPositionStream = events.ofType<DecrementAppointmentDate>()
        .filter { it.currentState != 0 }

    val updateDateStream = notFirstPositionStream
        .map { { ui: Ui -> ui.updateDisplayedDate(it.currentState.dec()) } }

    val enableButtonStream = notFirstPositionStream
        .map { { ui: Ui -> ui.enableDecrementButton(true) } }

    val firstPositionStream = events.ofType<DecrementAppointmentDate>()
        .filter { it.currentState == 0 }
        .map { { ui: Ui -> ui.enableDecrementButton(false) } }

    return Observable.merge(updateDateStream, enableButtonStream, firstPositionStream)
  }

  private fun schedulingSkips(events: Observable<UiEvent>): Observable<UiChange> {
    return events.ofType<SkipScheduling>()
        .map { { ui: Ui -> ui.closeSheet() } }
  }
}
