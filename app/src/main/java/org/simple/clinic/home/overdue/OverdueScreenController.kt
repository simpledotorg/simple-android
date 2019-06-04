package org.simple.clinic.home.overdue

import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.analytics.Analytics
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.patient.Age
import org.simple.clinic.user.UserSession
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.Period
import org.threeten.bp.ZoneOffset.UTC
import org.threeten.bp.temporal.ChronoUnit.DAYS
import javax.inject.Inject

typealias Ui = OverdueScreen
typealias UiChange = (Ui) -> Unit

class OverdueScreenController @Inject constructor(
    private val appointmentRepository: AppointmentRepository,
    private val userSession: UserSession,
    private val facilityRepository: FacilityRepository
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): Observable<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .compose(ReportAnalyticsEvents())
        .replay()

    return Observable.mergeArray(
        screenSetup(replayedEvents),
        markedAsAgreedToVisit(replayedEvents),
        rescheduleAppointment(replayedEvents),
        removeAppointment(replayedEvents),
        reportViewedPatientEvent(replayedEvents),
        openPhoneMaskBottomSheet(replayedEvents)
    )
  }

  private fun reportViewedPatientEvent(events: Observable<UiEvent>): Observable<UiChange> {
    return events.ofType<AppointmentExpanded>()
        .doOnNext { (patientUuid) -> Analytics.reportViewedPatient(patientUuid, OverdueScreenKey().analyticsName) }
        .flatMap { Observable.empty<UiChange>() }
  }

  private fun screenSetup(events: Observable<UiEvent>): Observable<UiChange> {
    val currentFacilityStream = userSession
        .requireLoggedInUser()
        .switchMap { facilityRepository.currentFacility(it) }

    val overdueAppointmentsStream = events
        .ofType<OverdueScreenCreated>()
        .withLatestFrom(currentFacilityStream)
        .flatMap { (_, currentFacility) -> appointmentRepository.overdueAppointments(currentFacility) }
        .replay()
        .refCount()

    val overduePatientsStream = overdueAppointmentsStream
        .map { appointments ->
          appointments.map {
            OverdueListItem.Patient(
                appointmentUuid = it.appointment.uuid,
                patientUuid = it.appointment.patientUuid,
                name = it.fullName,
                gender = it.gender,
                age = ageFromDateOfBirth(it.dateOfBirth, it.age),
                phoneNumber = it.phoneNumber?.number,
                bpSystolic = it.bloodPressure.systolic,
                bpDiastolic = it.bloodPressure.diastolic,
                bpDaysAgo = calculateDaysAgoFromInstant(it.bloodPressure.updatedAt),
                overdueDays = calculateOverdueDays(it.appointment.scheduledDate),
                isAtHighRisk = it.isAtHighRisk)
          }
        }
        .map { overduePatients ->
          { ui: Ui -> ui.updateList(overduePatients) }
        }

    val emptyStateStream = overdueAppointmentsStream
        .map { it.isEmpty() }
        .map { { ui: Ui -> ui.handleEmptyList(it) } }

    return Observable.merge(overduePatientsStream, emptyStateStream)
  }

  private fun ageFromDateOfBirth(dateOfBirth: LocalDate?, age: Age?): Int {
    return if (age == null) {
      Period.between(dateOfBirth!!, LocalDate.now(UTC)).years

    } else {
      val ageUpdatedAt = LocalDateTime.ofInstant(age.updatedAt, UTC)
      val updatedAtLocalDate = LocalDate.of(ageUpdatedAt.year, ageUpdatedAt.month, ageUpdatedAt.dayOfMonth)
      val yearsSinceThen = Period.between(updatedAtLocalDate, LocalDate.now(UTC)).years
      val ageWhenRecorded = age.value

      ageWhenRecorded + yearsSinceThen
    }
  }

  private fun calculateDaysAgoFromInstant(instant: Instant): Int {
    val localDateTime = LocalDateTime.ofInstant(instant, UTC)
    return calculateOverdueDays(LocalDate.of(localDateTime.year, localDateTime.month, localDateTime.dayOfMonth))
  }

  private fun calculateOverdueDays(date: LocalDate): Int {
    return DAYS.between(date, LocalDate.now(UTC)).toInt()
  }

  private fun openPhoneMaskBottomSheet(events: Observable<UiEvent>): Observable<UiChange> =
      events
          .ofType<CallPatientClicked>()
          .map { { ui: Ui -> ui.openPhoneMaskBottomSheet(it.patientUuid) } }

  private fun markedAsAgreedToVisit(events: Observable<UiEvent>): Observable<UiChange> {
    return events.ofType<AgreedToVisitClicked>()
        .flatMap {
          appointmentRepository
              .markAsAgreedToVisit(it.appointmentUuid)
              .toObservable<UiChange>()
        }
  }

  private fun rescheduleAppointment(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<RemindToCallLaterClicked>()
        .map { { ui: Ui -> ui.showAppointmentReminderSheet(it.appointmentUuid) } }
  }

  private fun removeAppointment(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<RemoveFromListClicked>()
        .map { { ui: Ui -> ui.showRemovePatientReasonSheet(it.appointmentUuid, it.patientUuid) } }
  }
}
