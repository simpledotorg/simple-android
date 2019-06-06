package org.simple.clinic.home.overdue.appointmentreminder

import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.user.User
import org.simple.clinic.user.UserSession
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneOffset
import org.threeten.bp.temporal.ChronoUnit
import javax.inject.Inject

typealias Ui = AppointmentReminderSheet
typealias UiChange = (Ui) -> Unit

class AppointmentReminderSheetController @Inject constructor(
    private val repository: AppointmentRepository,
    private val userSession: UserSession
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): Observable<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .compose(ReportAnalyticsEvents())
        .replay()

    return Observable.mergeArray(
        sheetCreates(replayedEvents),
        dateIncrements(replayedEvents),
        dateDecrements(replayedEvents),
        saveReminder(replayedEvents),
        closeSheetWhenUserBecomesUnauthorized())
  }

  private fun sheetCreates(events: Observable<UiEvent>): Observable<UiChange> {
    return events.ofType<AppointmentReminderSheetCreated>()
        .map { { ui: Ui -> ui.updateDisplayedDate(it.initialIndex) } }
  }

  private fun dateIncrements(events: Observable<UiEvent>): Observable<UiChange> {
    val isSecondLastItem = { index: Int, size: Int -> index == size - 2 }

    val updateDateStream = events.ofType<ReminderDateIncremented>()
        .map { { ui: Ui -> ui.updateDisplayedDate(newIndex = it.currentIndex.inc()) } }

    val firstItemStream = events.ofType<ReminderDateIncremented>()
        .filter { it.currentIndex == 0 }
        .map { { ui: Ui -> ui.enableDecrementButton(true) } }

    val secondLastItemStream = events.ofType<ReminderDateIncremented>()
        .filter { (current, size) -> isSecondLastItem(current, size) }
        .map { { ui: Ui -> ui.enableIncrementButton(false) } }

    val notSecondLastItemStream = events.ofType<ReminderDateIncremented>()
        .filter { (current, size) -> !isSecondLastItem(current, size) }
        .distinctUntilChanged()
        .map { { ui: Ui -> ui.enableIncrementButton(true) } }

    return Observable.merge(updateDateStream, firstItemStream, secondLastItemStream, notSecondLastItemStream)

  }

  private fun dateDecrements(events: Observable<UiEvent>): Observable<UiChange> {
    val isLastItem = { index: Int, size: Int -> index == size - 1 }

    val updateDateStream = events.ofType<ReminderDateDecremented>()
        .map { { ui: Ui -> ui.updateDisplayedDate(newIndex = it.currentIndex.dec()) } }

    val secondItemStream = events.ofType<ReminderDateDecremented>()
        .filter { it.currentIndex == 1 }
        .map { { ui: Ui -> ui.enableDecrementButton(false) } }

    val notSecondItemStream = events.ofType<ReminderDateDecremented>()
        .filter { it.currentIndex != 1 }
        .distinctUntilChanged()
        .map { { ui: Ui -> ui.enableDecrementButton(true) } }

    val lastItemStream = events.ofType<ReminderDateDecremented>()
        .filter { (current, size) -> isLastItem(current, size) }
        .map { { ui: Ui -> ui.enableIncrementButton(true) } }

    return Observable.merge(updateDateStream, secondItemStream, notSecondItemStream, lastItemStream)
  }

  private fun saveReminder(events: Observable<UiEvent>): Observable<UiChange> {
    val toLocalDate = { amount: Int, chronoUnit: ChronoUnit ->
      LocalDate.now(ZoneOffset.UTC).plus(amount.toLong(), chronoUnit)
    }

    val appointmentUuids = events
        .ofType<AppointmentReminderSheetCreated>()
        .map { it.appointmentUuid }

    return events
        .ofType<ReminderCreated>()
        .map { toLocalDate(it.selectedReminderState.timeAmount, it.selectedReminderState.chronoUnit) }
        .withLatestFrom(appointmentUuids)
        .flatMap { (date, uuid) ->
          repository
              .createReminder(appointmentUuid = uuid, reminderDate = date)
              .andThen(Observable.just { ui: Ui -> ui.closeSheet() })
        }
  }

  private fun closeSheetWhenUserBecomesUnauthorized(): Observable<UiChange> {
    return userSession
        .requireLoggedInUser()
        .filter { user -> user.loggedInStatus == User.LoggedInStatus.UNAUTHORIZED }
        .map { { ui: Ui -> ui.finish() } }
  }
}
