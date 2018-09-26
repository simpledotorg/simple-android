package org.simple.clinic.home.overdue.removepatient

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

typealias Ui = RemoveAppointmentSheet
typealias UiChange = (Ui) -> Unit

class RemoveAppointmentSheetController @Inject constructor(
    private val repository: AppointmentRepository
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(event: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = event.compose(ReportAnalyticsEvents()).replay().refCount()

    return Observable.merge(
        reasonClicks(replayedEvents),
        doneClicks(replayedEvents))
  }

  private fun reasonClicks(events: Observable<UiEvent>): Observable<UiChange> {
    val mergedReasons = Observable.merge(
        events.ofType<AlreadyVisitedReasonClicked>(),
        events.ofType<CancelReasonClicked>())

    return mergedReasons
        .distinctUntilChanged()
        .map { { ui: Ui -> ui.enableDoneButton() } }
  }

  private fun doneClicks(events: Observable<UiEvent>): Observable<UiChange> {
    val appointmentUuids = events.ofType<RemoveAppointmentSheetCreated>()
        .map { it.appointmentUuid }

    val alreadyVisitedClicks = events.ofType<AlreadyVisitedReasonClicked>()
    val cancelReasonClicks = events.ofType<CancelReasonClicked>()

    val mergedReasons = Observable.merge(alreadyVisitedClicks
        , cancelReasonClicks)

    val doneWithLatestFromReasons = events.ofType<RemoveReasonDoneClicked>()
        .withLatestFrom(appointmentUuids, mergedReasons)

    val markAsVisitedStream = doneWithLatestFromReasons
        .filter { (_, _, reason) -> reason is AlreadyVisitedReasonClicked }
        .flatMap { (_, uuid, _) ->
          repository.markAsVisited(uuid)
              .andThen(Observable.just { ui: Ui -> ui.closeSheet() })
        }

    val cancelWithReasonStream = doneWithLatestFromReasons
        .filter { (_, _, reason) -> reason is CancelReasonClicked }
        .flatMap { (_, uuid, reason) ->
          repository.cancelWithReason(uuid, (reason as CancelReasonClicked).selectedReason)
              .andThen(Observable.just { ui: Ui -> ui.closeSheet() })
        }

    return Observable.merge(markAsVisitedStream, cancelWithReasonStream)
  }
}
