package org.simple.clinic.summary

import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.cast
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import io.reactivex.rxkotlin.zipWith
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.analytics.Analytics
import org.simple.clinic.bp.BloodPressureRepository
import org.simple.clinic.overdue.Appointment
import org.simple.clinic.overdue.Appointment.Status.Cancelled
import org.simple.clinic.overdue.AppointmentCancelReason.InvalidPhoneNumber
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.summary.OpenIntention.LinkIdWithPatient
import org.simple.clinic.summary.OpenIntention.ViewExistingPatient
import org.simple.clinic.summary.OpenIntention.ViewNewPatient
import org.simple.clinic.summary.addphone.MissingPhoneReminderRepository
import org.simple.clinic.util.Just
import org.simple.clinic.util.None
import org.simple.clinic.util.exhaustive
import org.simple.clinic.util.filterAndUnwrapJust
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.Instant
import java.util.UUID

typealias Ui = PatientSummaryScreenUi
typealias UiChange = (Ui) -> Unit

class PatientSummaryScreenController @AssistedInject constructor(
    @Assisted private val patientUuid: UUID,
    @Assisted private val openIntention: OpenIntention,
    @Assisted private val screenCreatedTimestamp: Instant,
    private val patientRepository: PatientRepository,
    private val bpRepository: BloodPressureRepository,
    private val appointmentRepository: AppointmentRepository,
    private val missingPhoneReminderRepository: MissingPhoneReminderRepository
) : ObservableTransformer<UiEvent, UiChange> {

  @AssistedInject.Factory
  interface Factory {
    fun create(
        patientUuid: UUID,
        openIntention: OpenIntention,
        screenCreatedTimestamp: Instant
    ): PatientSummaryScreenController
  }

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events).replay()

    return Observable.mergeArray(
        reportViewedPatientEvent(replayedEvents),
        populatePatientProfile(),
        exitScreenAfterSchedulingAppointment(replayedEvents),
        openLinkIdWithPatientSheet(replayedEvents),
        showUpdatePhoneDialogIfRequired(replayedEvents),
        showScheduleAppointmentSheet(replayedEvents),
        goBackWhenBackClicked(replayedEvents),
        goToHomeOnDoneClick(replayedEvents),
        exitScreenIfLinkIdWithPatientIsCancelled(replayedEvents),
        hideLinkIdWithPatientSheet(replayedEvents)
    )
  }

  private fun reportViewedPatientEvent(events: Observable<UiEvent>): Observable<UiChange> {
    return events.ofType<ScreenCreated>()
        .take(1L)
        .doOnNext { Analytics.reportViewedPatient(patientUuid, openIntention.analyticsName()) }
        .flatMap { Observable.empty<UiChange>() }
  }

  private fun populatePatientProfile(): Observable<UiChange> {
    val sharedPatients = patientRepository.patient(patientUuid)
        .map {
          // We do not expect the patient to get deleted while this screen is already open.
          (it as Just).value
        }
        .replay(1)
        .refCount()

    val addresses = sharedPatients
        .flatMap { patient -> patientRepository.address(patient.addressUuid) }
        .map { (it as Just).value }

    val latestPhoneNumberStream = patientRepository.phoneNumber(patientUuid)
    val latestBpPassportStream = patientRepository.bpPassportForPatient(patientUuid)

    return Observables
        .combineLatest(sharedPatients, addresses, latestPhoneNumberStream, latestBpPassportStream) { patient, address, phoneNumber, bpPassport ->
          PatientSummaryProfile(patient, address, phoneNumber.toNullable(), bpPassport.toNullable())
        }
        .map { patientSummaryProfile -> { ui: Ui -> showPatientSummaryProfile(ui, patientSummaryProfile) } }
  }

  private fun showPatientSummaryProfile(ui: Ui, patientSummaryProfile: PatientSummaryProfile) {
    with(ui) {
      populatePatientProfile(patientSummaryProfile)
      showEditButton()
    }
  }

  private fun showScheduleAppointmentSheet(events: Observable<UiEvent>): Observable<UiChange> {
    val backClicks = events.ofType<PatientSummaryBackClicked>()
    val doneClicks = events.ofType<PatientSummaryDoneClicked>()

    val hasSummaryItemChangedStream = backClicks
        .map { patientRepository.hasPatientDataChangedSince(patientUuid, screenCreatedTimestamp) }

    val allBpsForPatientDeletedStream = backClicks
        .cast<UiEvent>()
        .mergeWith(doneClicks.cast())
        .map { doesNotHaveBloodPressures(patientUuid) }

    val shouldShowScheduleAppointmentSheetOnBackClicksStream = Observables
        .combineLatest(hasSummaryItemChangedStream, allBpsForPatientDeletedStream)
        .map { (hasSummaryItemChanged, allBpsForPatientDeleted) ->
          if (allBpsForPatientDeleted) false else hasSummaryItemChanged
        }

    val showScheduleAppointmentSheetOnBackClicks = backClicks
        .withLatestFrom(shouldShowScheduleAppointmentSheetOnBackClicksStream)
        .filter { (_, shouldShowScheduleAppointmentSheet) -> shouldShowScheduleAppointmentSheet }
        .map { (_, _) -> { ui: Ui -> ui.showScheduleAppointmentSheet(patientUuid) } }

    val showScheduleAppointmentSheetOnDoneClicks = doneClicks
        .withLatestFrom(allBpsForPatientDeletedStream)
        .filter { (_, allBpsForPatientDeleted) -> allBpsForPatientDeleted.not() }
        .map { (_, _) -> { ui: Ui -> ui.showScheduleAppointmentSheet(patientUuid) } }

    return showScheduleAppointmentSheetOnBackClicks
        .mergeWith(showScheduleAppointmentSheetOnDoneClicks)
  }

  private fun openLinkIdWithPatientSheet(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<ScreenCreated>()
        .filter { openIntention is LinkIdWithPatient }
        .map {
          val linkIdWithPatient = openIntention as LinkIdWithPatient
          { ui: Ui -> ui.showLinkIdWithPatientView(patientUuid, linkIdWithPatient.identifier) }
        }
  }

  private fun goBackWhenBackClicked(events: Observable<UiEvent>): Observable<UiChange> {
    val allBpsForPatientDeletedStream = events
        .ofType<PatientSummaryBackClicked>()
        .map { doesNotHaveBloodPressures(patientUuid) }

    val hasSummaryItemChangedStream = events
        .ofType<PatientSummaryBackClicked>()
        .map { patientRepository.hasPatientDataChangedSince(patientUuid, screenCreatedTimestamp) }

    val shouldGoBackStream = Observables
        .combineLatest(hasSummaryItemChangedStream, allBpsForPatientDeletedStream)
        .map { (hasSummaryItemChanged, allBpsForPatientDeleted) ->
          if (allBpsForPatientDeleted) true else hasSummaryItemChanged.not()
        }

    val shouldGoBackAfterBackClickedStream = events
        .ofType<PatientSummaryBackClicked>()
        .withLatestFrom(shouldGoBackStream) { _, shouldGoBack -> shouldGoBack }
        .filter { shouldGoBack -> shouldGoBack }

    val goBackToHomeScreen = shouldGoBackAfterBackClickedStream
        .filter { openIntention == ViewNewPatient || openIntention is LinkIdWithPatient }
        .map { { ui: Ui -> ui.goToHomeScreen() } }

    val goBackToSearchResults = shouldGoBackAfterBackClickedStream
        .filter { openIntention == ViewExistingPatient }
        .map { { ui: Ui -> ui.goToPreviousScreen() } }

    return goBackToHomeScreen.mergeWith(goBackToSearchResults)
  }

  private fun goToHomeOnDoneClick(events: Observable<UiEvent>): Observable<UiChange> {
    val allBpsForPatientDeletedStream = events
        .ofType<PatientSummaryDoneClicked>()
        .map { doesNotHaveBloodPressures(patientUuid) }

    return events
        .ofType<PatientSummaryDoneClicked>()
        .withLatestFrom(allBpsForPatientDeletedStream)
        .filter { (_, allBpsForPatientDeleted) -> allBpsForPatientDeleted }
        .map { { ui: Ui -> ui.goToHomeScreen() } }
  }

  private fun exitScreenAfterSchedulingAppointment(events: Observable<UiEvent>): Observable<UiChange> {
    val scheduleAppointmentCloses = events
        .ofType<ScheduleAppointmentSheetClosed>()

    val backClicks = events
        .ofType<PatientSummaryBackClicked>()

    val doneClicks = events
        .ofType<PatientSummaryDoneClicked>()

    val afterBackClicks = scheduleAppointmentCloses
        .withLatestFrom(backClicks)
        .map { (_, _) ->
          { ui: Ui ->
            when (openIntention) {
              ViewExistingPatient -> ui.goToPreviousScreen()
              ViewNewPatient, is LinkIdWithPatient -> ui.goToHomeScreen()
            }.exhaustive()
          }
        }

    val afterDoneClicks = scheduleAppointmentCloses
        .withLatestFrom(doneClicks)
        .map { { ui: Ui -> ui.goToHomeScreen() } }

    return afterBackClicks.mergeWith(afterDoneClicks)
  }

  private fun showUpdatePhoneDialogIfRequired(events: Observable<UiEvent>): Observable<UiChange> {
    val showForInvalidPhone = hasInvalidPhone(patientUuid)
        .take(1)
        .filter { invalid -> invalid }
        .map { { ui: Ui -> ui.showUpdatePhoneDialog(patientUuid) } }

    val waitTillABpIsRecorded = events
        .ofType<PatientSummaryBloodPressureSaved>()
        .take(1)

    val showForMissingPhone = waitTillABpIsRecorded
        .filter { openIntention != ViewNewPatient }
        .switchMap {
          isMissingPhoneAndShouldBeReminded(patientUuid)
              .take(1)
              .filter { missing -> missing }
              .flatMap {
                missingPhoneReminderRepository
                    .markReminderAsShownFor(patientUuid)
                    .andThen(Observable.just { ui: Ui -> ui.showAddPhoneDialog(patientUuid) })
              }
        }

    return showForInvalidPhone.mergeWith(showForMissingPhone)
  }

  private fun hasInvalidPhone(patientUuid: UUID): Observable<Boolean> {
    return patientRepository.phoneNumber(patientUuid)
        .filterAndUnwrapJust()
        .zipWith(lastCancelledAppointmentWithInvalidPhone(patientUuid))
        .map { (number, appointment) -> appointment.updatedAt > number.updatedAt }
  }

  private fun isMissingPhoneAndShouldBeReminded(patientUuid: UUID): Observable<Boolean> {
    return patientRepository
        .phoneNumber(patientUuid)
        .zipWith(hasShownReminderForMissingPhone(patientUuid))
        .map { (number, reminderShown) -> number is None && reminderShown.not() }
  }

  private fun lastCancelledAppointmentWithInvalidPhone(patientUuid: UUID): Observable<Appointment> {
    return appointmentRepository
        .lastCreatedAppointmentForPatient(patientUuid)
        .filterAndUnwrapJust()
        .filter { it.status == Cancelled && it.cancelReason == InvalidPhoneNumber }
  }

  private fun hasShownReminderForMissingPhone(patientUuid: UUID): Observable<Boolean> {
    return missingPhoneReminderRepository
        .hasShownReminderFor(patientUuid)
        .toObservable()
  }

  private fun exitScreenIfLinkIdWithPatientIsCancelled(events: Observable<UiEvent>): Observable<UiChange> {
    val screenCreates = events.ofType<ScreenCreated>()
    val linkIdCancelled = events.ofType<PatientSummaryLinkIdCancelled>()

    return Observables.combineLatest(screenCreates, linkIdCancelled)
        .take(1)
        .map { { ui: Ui -> ui.goToPreviousScreen() } }
  }

  private fun hideLinkIdWithPatientSheet(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<PatientSummaryLinkIdCompleted>()
        .map { Ui::hideLinkIdWithPatientView }
  }

  private fun doesNotHaveBloodPressures(patientUuid: UUID): Boolean {
    return bpRepository.bloodPressureCount(patientUuid) == 0
  }
}
