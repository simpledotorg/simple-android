package org.simple.clinic.scheduleappointment

import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.Single
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.overdue.Appointment.AppointmentType.Automatic
import org.simple.clinic.overdue.Appointment.AppointmentType.Manual
import org.simple.clinic.overdue.AppointmentConfig
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.UtcClock
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.LocalDate
import org.threeten.bp.temporal.ChronoUnit.DAYS
import javax.inject.Inject

typealias Ui = ScheduleAppointmentSheet
typealias UiChange = (Ui) -> Unit

class ScheduleAppointmentSheetController @Inject constructor(
    private val appointmentRepository: AppointmentRepository,
    private val patientRepository: PatientRepository,
    private val configProvider: Single<AppointmentConfig>,
    private val utcClock: UtcClock,
    private val userSession: UserSession,
    private val facilityRepository: FacilityRepository
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): Observable<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .compose(ReportAnalyticsEvents())
        .replay()

    return Observable.mergeArray(
        setupDefaultState(replayedEvents),
        dateIncrements(replayedEvents),
        dateDecrements(replayedEvents),
        schedulingSkips(replayedEvents),
        scheduleCreates(replayedEvents),
        incrementDecrements(replayedEvents),
        enableIncrements(replayedEvents),
        enableDecrements(replayedEvents),
        showSelectedCalendarDate(replayedEvents),
        showCalendar(replayedEvents)
    )
  }

  private fun incrementDecrements(events: Observable<UiEvent>): Observable<UiChange> =
      latestIndex(events)
          .withLatestFrom(possibleAppointments(events)) { latestIndex, possibleAppointments ->
            possibleAppointments[latestIndex]
          }
          .distinctUntilChanged()
          .map { appointment -> { ui: Ui -> ui.updateScheduledAppointment(appointment) } }

  private fun enableIncrements(events: Observable<UiEvent>): Observable<UiChange> =
      latestIndex(events)
          .withLatestFrom(possibleAppointments(events)) { latestIndex, possibleAppointments ->
            latestIndex < possibleAppointments.lastIndex
          }
          .distinctUntilChanged()
          .map { enable -> { ui: Ui -> ui.enableIncrementButton(enable) } }

  private fun enableDecrements(events: Observable<UiEvent>): Observable<UiChange> =
      latestIndex(events)
          .map { it > 0 }
          .distinctUntilChanged()
          .map { enable -> { ui: Ui -> ui.enableDecrementButton(enable) } }

  private fun showSelectedCalendarDate(events: Observable<UiEvent>): Observable<UiChange> =
      events
          .ofType<AppointmentCalendarDateSelected>()
          .map {
            val days = DAYS.between(LocalDate.now(utcClock), LocalDate.of(it.year, it.month, it.dayOfMonth))
            ScheduleAppointment(
                displayText = "$days days",
                timeAmount = days.toInt(),
                chronoUnit = DAYS
            )
          }
          .map { { ui: Ui -> ui.updateScheduledAppointment(it) } }

  private fun showCalendar(events: Observable<UiEvent>): Observable<UiChange> =
      events
          .ofType<AppointmentChooseCalendarClicks>()
          .withLatestFrom(latestAppointment(events)) { _, appointment ->
            { ui: Ui ->
              ui.showCalendar(LocalDate.now(utcClock).plus(appointment.timeAmount.toLong(), appointment.chronoUnit))
            }
          }

  private fun latestAppointment(events: Observable<UiEvent>) =
      latestIndex(events)
          .withLatestFrom(possibleAppointments(events)) { latestIndex, possibleAppointments ->
            possibleAppointments[latestIndex]
          }

  private fun latestIndex(events: Observable<UiEvent>) =
      latestDateAction(events)
          .scan(0) { index, action ->
            when (action) {
              AppointmentDateIncremented2 -> index + 1
              AppointmentDateDecremented2 -> index - 1
              is ScheduleAppointmentSheetCreated2 -> action.possibleAppointments.indexOf(action.defaultAppointment)
              else -> index
            }
          }
          .withLatestFrom(possibleAppointments(events)) { latestIndex, possibleAppointments ->
            latestIndex.coerceIn(0, possibleAppointments.lastIndex)
          }

  private fun latestDateAction(events: Observable<UiEvent>) =
      Observable.merge(
          events.ofType<AppointmentDateIncremented2>(),
          events.ofType<AppointmentDateDecremented2>(),
          events.ofType<ScheduleAppointmentSheetCreated2>()
      )

  private fun possibleAppointments(events: Observable<UiEvent>) =
      events
          .ofType<ScheduleAppointmentSheetCreated2>()
          .map { it.possibleAppointments }

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
        .switchMap { (_, patientUuid) -> patientRepository.isPatientDefaulter(patientUuid) }
        .replay()
        .refCount()

    val currentFacilityStream = userSession
        .requireLoggedInUser()
        .switchMap { user -> facilityRepository.currentFacility(user) }

    val saveAppointmentAndCloseSheet = isPatientDefaulterStream
        .filter { isPatientDefaulter -> isPatientDefaulter }
        .withLatestFrom(patientUuidStream, configProvider.toObservable(), currentFacilityStream) { _, patientUuid, config, currentFacilty ->
          Triple(patientUuid, config, currentFacilty)
        }
        .flatMapSingle { (patientUuid, config, currentFacility) ->
          appointmentRepository
              .schedule(
                  patientUuid = patientUuid,
                  appointmentDate = LocalDate.now(utcClock).plus(config.appointmentDuePeriodForDefaulters),
                  appointmentType = Automatic,
                  currentFacility = currentFacility)
              .map { { ui: Ui -> ui.closeSheet() } }
        }

    val closeSheetWithoutSavingAppointment = isPatientDefaulterStream
        .filter { isPatientDefaulter -> isPatientDefaulter.not() }
        .map { { ui: Ui -> ui.closeSheet() } }

    return Observable.merge(saveAppointmentAndCloseSheet, closeSheetWithoutSavingAppointment)
  }

  private fun scheduleCreates(events: Observable<UiEvent>): Observable<UiChange> {
    val toLocalDate = { appointment: ScheduleAppointment ->
      LocalDate.now(utcClock).plus(appointment.timeAmount.toLong(), appointment.chronoUnit)
    }

    val patientUuidStream = events.ofType<ScheduleAppointmentSheetCreated>()
        .map { it.patientUuid }

    val currentFacilityStream = userSession
        .requireLoggedInUser()
        .switchMap { user -> facilityRepository.currentFacility(user) }

    return events.ofType<AppointmentScheduled>()
        .map { toLocalDate(it.selectedDateState) }
        .withLatestFrom(patientUuidStream, currentFacilityStream)
        .flatMapSingle { (date, uuid, currentFacility) ->
          appointmentRepository
              .schedule(patientUuid = uuid, appointmentDate = date, appointmentType = Manual, currentFacility = currentFacility)
              .map { { ui: Ui -> ui.closeSheet() } }
        }
  }
}
