package org.simple.clinic.scheduleappointment

import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneOffset.UTC
import org.threeten.bp.temporal.ChronoUnit
import javax.inject.Inject

typealias Ui = ScheduleAppointmentSheet
typealias UiChange = (Ui) -> Unit

class ScheduleAppointmentSheetController @Inject constructor(
    private val repository: AppointmentRepository
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(upstream: Observable<UiEvent>): Observable<UiChange> {
    val replayedEvents = upstream.compose(ReportAnalyticsEvents()).replay().refCount()

    return Observable.mergeArray(
        sheetCreates(replayedEvents),
        dateIncrements(replayedEvents),
        dateDecrements(replayedEvents),
        schedulingSkips(replayedEvents),
        scheduleCreates(replayedEvents))
  }

  private fun sheetCreates(events: Observable<UiEvent>): Observable<UiChange> {
    return events.ofType<ScheduleAppointmentSheetCreated>()
        .map { { ui: Ui -> ui.updateDisplayedDate(it.initialIndex) } }
  }

  private fun dateIncrements(events: Observable<UiEvent>): Observable<UiChange> {
    val isSecondLastItem = { index: Int, size: Int -> index == size - 2 }

    val updateDateStream = events.ofType<AppointmentDateIncremented>()
        .map { { ui: Ui -> ui.updateDisplayedDate(newIndex = it.currentIndex.inc()) } }

    val firstItemStream = events.ofType<AppointmentDateIncremented>()
        .filter { it.currentIndex == 0 }
        .map { { ui: Ui -> ui.enableDecrementButton(true) } }

    val secondLastItemStream = events.ofType<AppointmentDateIncremented>()
        .filter { (current, size) -> isSecondLastItem(current, size) }
        .map { { ui: Ui -> ui.enableIncrementButton(false) } }

    val notSecondLastItemStream = events.ofType<AppointmentDateIncremented>()
        .filter { (current, size) -> !isSecondLastItem(current, size) }
        .distinctUntilChanged()
        .map { { ui: Ui -> ui.enableIncrementButton(true) } }

    return Observable.merge(updateDateStream, firstItemStream, secondLastItemStream, notSecondLastItemStream)
  }

  private fun dateDecrements(events: Observable<UiEvent>): Observable<UiChange> {
    val isLastItem = { index: Int, size: Int -> index == size - 1 }

    val updateDateStream = events.ofType<AppointmentDateDecremented>()
        .map { { ui: Ui -> ui.updateDisplayedDate(newIndex = it.currentIndex.dec()) } }

    val secondItemStream = events.ofType<AppointmentDateDecremented>()
        .filter { it.currentIndex == 1 }
        .map { { ui: Ui -> ui.enableDecrementButton(false) } }

    val notSecondItemStream = events.ofType<AppointmentDateDecremented>()
        .filter { it.currentIndex != 1 }
        .distinctUntilChanged()
        .map { { ui: Ui -> ui.enableDecrementButton(true) } }

    val lastItemStream = events.ofType<AppointmentDateDecremented>()
        .filter { (current, size) -> isLastItem(current, size) }
        .map { { ui: Ui -> ui.enableIncrementButton(true) } }

    return Observable.merge(updateDateStream, secondItemStream, notSecondItemStream, lastItemStream)
  }

  private fun schedulingSkips(events: Observable<UiEvent>): Observable<UiChange> {
    return events.ofType<SchedulingSkipped>()
        .map { { ui: Ui -> ui.closeSheet() } }
  }

  private fun scheduleCreates(events: Observable<UiEvent>): Observable<UiChange> {
    val toLocalDate = { pair: Pair<Int, ChronoUnit> ->
      LocalDate.now(UTC).plus(pair.first.toLong(), pair.second)
    }

    val patientUuidStream = events.ofType<ScheduleAppointmentSheetCreated>()
        .map { it.patientUuid }

    return events.ofType<AppointmentScheduled>()
        .map { toLocalDate(it.selectedDateState.second) }
        .withLatestFrom(patientUuidStream)
        .flatMap { (date, uuid) ->
          repository.schedule(patientUuid = uuid, appointmentDate = date)
              .andThen(Observable.just { ui: Ui -> ui.closeSheet() })
        }
  }
}
