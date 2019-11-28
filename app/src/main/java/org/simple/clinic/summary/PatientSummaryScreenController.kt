package org.simple.clinic.summary

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.cast
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import io.reactivex.rxkotlin.zipWith
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.analytics.Analytics
import org.simple.clinic.bp.BloodPressureRepository
import org.simple.clinic.drugs.PrescriptionRepository
import org.simple.clinic.medicalhistory.Answer
import org.simple.clinic.medicalhistory.MedicalHistory
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.DIAGNOSED_WITH_HYPERTENSION
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HAS_DIABETES
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HAS_HAD_A_HEART_ATTACK
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HAS_HAD_A_KIDNEY_DISEASE
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HAS_HAD_A_STROKE
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.IS_ON_TREATMENT_FOR_HYPERTENSION
import org.simple.clinic.medicalhistory.MedicalHistoryRepository
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
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.exhaustive
import org.simple.clinic.util.filterAndUnwrapJust
import org.simple.clinic.widgets.UiEvent
import java.util.UUID
import javax.inject.Inject

typealias Ui = PatientSummaryScreenUi
typealias UiChange = (Ui) -> Unit

class PatientSummaryScreenController @Inject constructor(
    private val patientRepository: PatientRepository,
    private val bpRepository: BloodPressureRepository,
    private val prescriptionRepository: PrescriptionRepository,
    private val medicalHistoryRepository: MedicalHistoryRepository,
    private val appointmentRepository: AppointmentRepository,
    private val missingPhoneReminderRepository: MissingPhoneReminderRepository,
    private val config: PatientSummaryConfig
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .compose(mergeWithPatientSummaryChanges())
        .compose(ReportAnalyticsEvents())
        .replay()

    return Observable.mergeArray(
        populateList(replayedEvents),
        reportViewedPatientEvent(replayedEvents),
        populatePatientProfile(replayedEvents),
        updateMedicalHistory(replayedEvents),
        openBloodPressureBottomSheet(replayedEvents),
        openPrescribedDrugsScreen(replayedEvents),
        exitScreenAfterSchedulingAppointment(replayedEvents),
        openBloodPressureUpdateSheet(replayedEvents),
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
    return events.ofType<PatientSummaryScreenCreated>()
        .take(1L)
        .doOnNext { (patientUuid, openIntention) -> Analytics.reportViewedPatient(patientUuid, openIntention.analyticsName()) }
        .flatMap { Observable.empty<UiChange>() }
  }

  private fun populatePatientProfile(events: Observable<UiEvent>): Observable<UiChange> {
    val patientUuid = events
        .ofType<PatientSummaryScreenCreated>()
        .map { it.patientUuid }

    val sharedPatients = patientUuid
        .flatMap { patientRepository.patient(it) }
        .map {
          // We do not expect the patient to get deleted while this screen is already open.
          (it as Just).value
        }
        .replay(1)
        .refCount()

    val addresses = sharedPatients
        .flatMap { patient -> patientRepository.address(patient.addressUuid) }
        .map { (it as Just).value }

    val phoneNumbers = patientUuid
        .flatMap { patientRepository.phoneNumber(it) }

    val bpPassport = patientUuid
        .flatMap { patientRepository.bpPassportForPatient(it) }

    return Observables
        .combineLatest(sharedPatients, addresses, phoneNumbers, bpPassport, ::PatientSummaryProfile)
        .map { patientSummaryProfile -> { ui: Ui -> showPatientSummaryProfile(ui, patientSummaryProfile) } }
  }

  private fun showPatientSummaryProfile(ui: Ui, patientSummaryProfile: PatientSummaryProfile) {
    with(ui) {
      populatePatientProfile(patientSummaryProfile)
      showEditButton()
    }
  }

  private fun mergeWithPatientSummaryChanges(): ObservableTransformer<UiEvent, UiEvent> {
    return ObservableTransformer { events ->
      val patientUuids = events
          .ofType<PatientSummaryScreenCreated>()
          .map { it.patientUuid }
          .distinctUntilChanged()

      val prescribedDrugsStream = patientUuids.flatMap { prescriptionRepository.newestPrescriptionsForPatient(it) }

      val bloodPressures = patientUuids.flatMap { patientUuid -> bpRepository.newestMeasurementsForPatient(patientUuid, config.numberOfBpsToDisplay) }

      val medicalHistoryItems = patientUuids.flatMap { medicalHistoryRepository.historyForPatientOrDefault(it) }

      // combineLatest() is important here so that the first data-set for the list
      // is dispatched in one go instead of them appearing one after another on the UI.
      val summaryItemChanges = Observables
          .combineLatest(prescribedDrugsStream, bloodPressures, medicalHistoryItems) { prescribedDrugs, bloodPressureMeasurements, history ->
            PatientSummaryItemChanged(PatientSummaryItems(
                prescription = prescribedDrugs,
                bloodPressures = bloodPressureMeasurements,
                medicalHistory = history
            ))
          }
          .distinctUntilChanged()

      events.mergeWith(summaryItemChanges)
    }
  }

  private fun populateList(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<PatientSummaryItemChanged>()
        .map { it.patientSummaryItems }
        .map { patientSummary ->
          { ui: Ui -> ui.populateList(patientSummary.prescription, patientSummary.bloodPressures, patientSummary.medicalHistory) }
        }
  }

  private fun updateMedicalHistory(events: Observable<UiEvent>): Observable<UiChange> {
    val patientUuids = events
        .ofType<PatientSummaryScreenCreated>()
        .map { it.patientUuid }

    val medicalHistories = patientUuids
        .flatMap { medicalHistoryRepository.historyForPatientOrDefault(it) }

    val updateHistory = { medicalHistory: MedicalHistory, question: MedicalHistoryQuestion, answer: Answer ->
      when (question) {
        DIAGNOSED_WITH_HYPERTENSION -> medicalHistory.copy(diagnosedWithHypertension = answer)
        IS_ON_TREATMENT_FOR_HYPERTENSION -> medicalHistory.copy(isOnTreatmentForHypertension = answer)
        HAS_HAD_A_HEART_ATTACK -> medicalHistory.copy(hasHadHeartAttack = answer)
        HAS_HAD_A_STROKE -> medicalHistory.copy(hasHadStroke = answer)
        HAS_HAD_A_KIDNEY_DISEASE -> medicalHistory.copy(hasHadKidneyDisease = answer)
        HAS_DIABETES -> medicalHistory.copy(hasDiabetes = answer)
      }
    }

    return events.ofType<SummaryMedicalHistoryAnswerToggled>()
        .withLatestFrom(medicalHistories)
        .map { (toggleEvent, medicalHistory) ->
          updateHistory(medicalHistory, toggleEvent.question, toggleEvent.answer)
        }
        .flatMap {
          medicalHistoryRepository
              .save(it)
              .andThen(Observable.never<UiChange>())
        }
  }

  private fun openBloodPressureBottomSheet(events: Observable<UiEvent>): Observable<UiChange> {
    val patientUuid = events
        .ofType<PatientSummaryScreenCreated>()
        .map { it.patientUuid }

    return events
        .ofType<PatientSummaryNewBpClicked>()
        .withLatestFrom(patientUuid)
        .map { (_, patientUuid) -> { ui: Ui -> ui.showBloodPressureEntrySheet(patientUuid) } }
  }

  private fun openPrescribedDrugsScreen(events: Observable<UiEvent>): Observable<UiChange> {
    val patientUuid = events
        .ofType<PatientSummaryScreenCreated>()
        .map { it.patientUuid }

    return events
        .ofType<PatientSummaryUpdateDrugsClicked>()
        .withLatestFrom(patientUuid)
        .map { (_, patientUuid) ->
          { ui: Ui -> ui.showUpdatePrescribedDrugsScreen(patientUuid) }
        }
  }

  private fun showScheduleAppointmentSheet(events: Observable<UiEvent>): Observable<UiChange> {
    val screenCreatedEvents = events.ofType<PatientSummaryScreenCreated>()

    val patientUuids = screenCreatedEvents.map { it.patientUuid }

    val backClicks = events.ofType<PatientSummaryBackClicked>()
    val doneClicks = events.ofType<PatientSummaryDoneClicked>()

    val hasSummaryItemChangedStream = backClicks
        .zipWith(screenCreatedEvents) { _, screenCreated -> screenCreated.patientUuid to screenCreated.screenCreatedTimestamp }
        .map { (patientUuid, screenCreatedAt) -> patientRepository.hasPatientDataChangedSince(patientUuid, screenCreatedAt) }

    val allBpsForPatientDeletedStream = backClicks
        .cast<UiEvent>()
        .mergeWith(doneClicks.cast())
        .zipWith(screenCreatedEvents) { _, screenCreated -> screenCreated.patientUuid }
        .map(::doesNotHaveBloodPressures)

    val shouldShowScheduleAppointmentSheetOnBackClicksStream = Observables
        .combineLatest(hasSummaryItemChangedStream, allBpsForPatientDeletedStream)
        .map { (hasSummaryItemChanged, allBpsForPatientDeleted) ->
          if (allBpsForPatientDeleted) false else hasSummaryItemChanged
        }

    val showScheduleAppointmentSheetOnBackClicks = backClicks
        .withLatestFrom(shouldShowScheduleAppointmentSheetOnBackClicksStream, patientUuids)
        .filter { (_, shouldShowScheduleAppointmentSheet, _) -> shouldShowScheduleAppointmentSheet }
        .map { (_, _, uuid) -> { ui: Ui -> ui.showScheduleAppointmentSheet(patientUuid = uuid) } }

    val showScheduleAppointmentSheetOnDoneClicks = doneClicks
        .withLatestFrom(allBpsForPatientDeletedStream, patientUuids)
        .filter { (_, allBpsForPatientDeleted, _) -> allBpsForPatientDeleted.not() }
        .map { (_, _, uuid) -> { ui: Ui -> ui.showScheduleAppointmentSheet(patientUuid = uuid) } }

    return showScheduleAppointmentSheetOnBackClicks
        .mergeWith(showScheduleAppointmentSheetOnDoneClicks)
  }

  private fun openLinkIdWithPatientSheet(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<PatientSummaryScreenCreated>()
        .filter { it.openIntention is LinkIdWithPatient }
        .map {
          val linkIdWithPatient = it.openIntention as LinkIdWithPatient
          { ui: Ui -> ui.showLinkIdWithPatientView(it.patientUuid, linkIdWithPatient.identifier) }
        }
  }

  private fun goBackWhenBackClicked(events: Observable<UiEvent>): Observable<UiChange> {
    val screenCreatedEvents = events.ofType<PatientSummaryScreenCreated>()

    val openIntentions = screenCreatedEvents.map { it.openIntention }

    val allBpsForPatientDeletedStream = events.ofType<PatientSummaryBackClicked>()
        .zipWith(screenCreatedEvents) { _, screenCreated -> screenCreated.patientUuid }
        .map(::doesNotHaveBloodPressures)

    val hasSummaryItemChangedStream = events.ofType<PatientSummaryBackClicked>()
        .zipWith(screenCreatedEvents) { _, screenCreated -> screenCreated.patientUuid to screenCreated.screenCreatedTimestamp }
        .map { (patientUuid, screenCreatedAt) -> patientRepository.hasPatientDataChangedSince(patientUuid, screenCreatedAt) }

    val shouldGoBackStream = Observables
        .combineLatest(hasSummaryItemChangedStream, allBpsForPatientDeletedStream)
        .map { (hasSummaryItemChanged, allBpsForPatientDeleted) ->
          if (allBpsForPatientDeleted) true else hasSummaryItemChanged.not()
        }

    val shouldGoBackWithIntentionStream = events
        .ofType<PatientSummaryBackClicked>()
        .withLatestFrom(shouldGoBackStream, openIntentions) { _, shouldGoBack, openIntention ->
          shouldGoBack to openIntention
        }
        .filter { (shouldGoBack, _) -> shouldGoBack }

    val goBackToHomeScreen = shouldGoBackWithIntentionStream
        .filter { (_, openIntention) -> openIntention == ViewNewPatient || openIntention is LinkIdWithPatient }
        .map { { ui: Ui -> ui.goToHomeScreen() } }

    val goBackToSearchResults = shouldGoBackWithIntentionStream
        .filter { (_, openIntention) -> openIntention == ViewExistingPatient }
        .map { { ui: Ui -> ui.goToPreviousScreen() } }

    return goBackToHomeScreen.mergeWith(goBackToSearchResults)
  }

  private fun goToHomeOnDoneClick(events: Observable<UiEvent>): Observable<UiChange> {
    val allBpsForPatientDeletedStream = events.ofType<PatientSummaryDoneClicked>()
        .zipWith(events.ofType<PatientSummaryScreenCreated>()) { _, screenCreated -> screenCreated.patientUuid }
        .map(::doesNotHaveBloodPressures)

    return events
        .ofType<PatientSummaryDoneClicked>()
        .withLatestFrom(allBpsForPatientDeletedStream)
        .filter { (_, allBpsForPatientDeleted) -> allBpsForPatientDeleted }
        .map { { ui: Ui -> ui.goToHomeScreen() } }
  }

  private fun exitScreenAfterSchedulingAppointment(events: Observable<UiEvent>): Observable<UiChange> {
    val openIntentions = events
        .ofType<PatientSummaryScreenCreated>()
        .map { it.openIntention }

    val scheduleAppointmentCloses = events
        .ofType<ScheduleAppointmentSheetClosed>()

    val backClicks = events
        .ofType<PatientSummaryBackClicked>()

    val doneClicks = events
        .ofType<PatientSummaryDoneClicked>()

    val afterBackClicks = scheduleAppointmentCloses
        .withLatestFrom(backClicks, openIntentions)
        .map { (_, _, openIntention) ->
          { ui: Ui ->
            when (openIntention!!) {
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

  private fun openBloodPressureUpdateSheet(events: Observable<UiEvent>): Observable<UiChange> {
    return events.ofType<PatientSummaryBpClicked>()
        .map { it.bloodPressureMeasurement }
        .map { bp -> { ui: Ui -> ui.showBloodPressureUpdateSheet(bp.uuid) } }
  }

  private fun showUpdatePhoneDialogIfRequired(events: Observable<UiEvent>): Observable<UiChange> {
    val screenCreations = events.ofType<PatientSummaryScreenCreated>()

    val showForInvalidPhone = screenCreations
        .map { it.patientUuid }
        .switchMap { patientUuid ->
          hasInvalidPhone(patientUuid)
              .take(1)
              .filter { invalid -> invalid }
              .map { { ui: Ui -> ui.showUpdatePhoneDialog(patientUuid) } }
        }

    val waitTillABpIsRecorded = events
        .ofType<PatientSummaryBloodPressureSaved>()
        .take(1)

    val showForMissingPhone = Observables
        .combineLatest(screenCreations, waitTillABpIsRecorded) { screenCreated, _ -> screenCreated }
        .filter { it.openIntention != ViewNewPatient }
        .map { it.patientUuid }
        .switchMap { patientUuid ->
          isMissingPhoneAndShouldBeReminded(patientUuid)
              .take(1)
              .filter { missing -> missing }
              .flatMap {
                missingPhoneReminderRepository
                    .markReminderAsShownFor(patientUuid)
                    .andThen(Observable.just({ ui: Ui -> ui.showAddPhoneDialog(patientUuid) }))
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
    val screenCreates = events.ofType<PatientSummaryScreenCreated>()
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
