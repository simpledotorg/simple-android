package org.simple.clinic.scheduleappointment

import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.Single
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.overdue.Appointment
import org.simple.clinic.overdue.Appointment.AppointmentType.*
import org.simple.clinic.overdue.AppointmentConfig
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.util.UtcClock
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.LocalDate
import javax.inject.Inject

typealias Ui = ScheduleAppointmentSheet
typealias UiChange = (Ui) -> Unit

class ScheduleAppointmentSheetController @Inject constructor(
    private val appointmentRepository: AppointmentRepository,
    private val patientRepository: PatientRepository,
    private val configProvider: Single<AppointmentConfig>,
    private val utcClock: UtcClock
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(upstream: Observable<UiEvent>): Observable<UiChange> {
    val replayedEvents = upstream.compose(ReportAnalyticsEvents()).replay().refCount()

    return Observable.mergeArray(
        setupDefaultState(replayedEvents),
        dateIncrements(replayedEvents),
        dateDecrements(replayedEvents),
        schedulingSkips(replayedEvents),
        scheduleCreates(replayedEvents))
  }

  private fun setupDefaultState(events: Observable<UiEvent>): Observable<UiChange> {
    return events.ofType<ScheduleAppointmentSheetCreated>()
        .map {
          { ui: Ui ->
            val isShowingLastItem = it.defaultDateIndex == it.numberOfDates - 1
            val isShowingFirstItem = it.defaultDateIndex == 0

            ui.updateDisplayedDate(it.defaultDateIndex)
            ui.enableIncrementButton(isShowingLastItem.not())
            ui.enableDecrementButton(isShowingFirstItem.not())
          }
        }
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
    val patientUuidStream = events
        .ofType<ScheduleAppointmentSheetCreated>()
        .map { it.patientUuid }

    val combinedStreams = Observables.combineLatest(
        events.ofType<SchedulingSkipped>(),
        patientUuidStream,
        configProvider.toObservable()
    )
    val isPatientDefaulterStream = combinedStreams
        .filter { (_, _, config) -> config.isApiV3Enabled }
        .switchMap { (_, patientUuid) ->
          patientRepository.isPatientDefaulter(patientUuid)
        }
        .replay()
        .refCount()

    val saveAppointmentAndCloseSheet = isPatientDefaulterStream
        .withLatestFrom(patientUuidStream, configProvider.toObservable())
        .filter { (isPatientDefaulter, _, _) -> isPatientDefaulter }
        .flatMapSingle { (_, patientUuid, config) ->
          appointmentRepository
              .schedule(
                  patientUuid = patientUuid,
                  appointmentDate = LocalDate.now(utcClock).plus(config.appointmentDuePeriodForDefaulters),
                  appointmentType = Automatic)
              .map { { ui: Ui -> ui.closeSheet() } }
        }

    val closeSheetWithoutSavingAppointment = isPatientDefaulterStream
        .filter { isPatientDefaulter -> isPatientDefaulter.not() }
        .map { { ui: Ui -> ui.closeSheet() } }

    val defaulterFeatureDisabledStream = combinedStreams
        .filter { (_, _, config) -> config.isApiV3Enabled.not() }
        .map { { ui: Ui -> ui.closeSheet() } }

    return Observable.merge(saveAppointmentAndCloseSheet, closeSheetWithoutSavingAppointment, defaulterFeatureDisabledStream)
  }

  private fun scheduleCreates(events: Observable<UiEvent>): Observable<UiChange> {
    val toLocalDate = { appointment: ScheduleAppointment ->
      LocalDate.now(utcClock).plus(appointment.timeAmount.toLong(), appointment.chronoUnit)
    }

    val patientUuidStream = events.ofType<ScheduleAppointmentSheetCreated>()
        .map { it.patientUuid }

    return events.ofType<AppointmentScheduled>()
        .map { toLocalDate(it.selectedDateState) }
        .withLatestFrom(patientUuidStream)
        .flatMapSingle { (date, uuid) ->
          appointmentRepository
              .schedule(patientUuid = uuid, appointmentDate = date)
              .map { { ui: Ui -> ui.closeSheet(date) } }
        }
  }
}
