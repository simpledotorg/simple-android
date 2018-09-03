package org.simple.clinic.home.overdue

import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneOffset
import org.threeten.bp.temporal.ChronoUnit.DAYS
import javax.inject.Inject

typealias Ui = OverdueScreen
typealias UiChange = (Ui) -> Unit

class OverdueScreenController @Inject constructor(
    private val appointmentRepo: AppointmentRepository
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(upstream: Observable<UiEvent>): Observable<UiChange> {
    val replayedEvents = upstream.replay().refCount()

    return screenSetup(replayedEvents)
  }

  private fun screenSetup(events: Observable<UiEvent>): Observable<UiChange> {
    val dbStream = events
        .ofType<OverdueScreenCreated>()
        .flatMap { appointmentRepo.overdueAppointments() }

    val updateListStream = dbStream
        .map { appointments ->
          appointments.map {
            OverdueListItem(
                appointmentUuid = it.uuid,
                name = it.patientUuid.toString(),
                gender = "Female",
                age = 22,
                bpSystolic = 175,
                bpDiastolic = 55,
                bpDaysAgo = 30,
                overdueDays = getOverdueDays(it.date))
          }
        }
        .map { { ui: Ui -> ui.updateList(it) } }

    val emptyStateStream = dbStream
        .map { it.isEmpty() }
        .map { { ui: Ui -> ui.handleEmptyList(it) } }

    return updateListStream.mergeWith(emptyStateStream)
  }

  private fun getOverdueDays(date: LocalDate): Int {
    return DAYS.between(date, LocalDate.now(ZoneOffset.UTC)).toInt()
  }
}
