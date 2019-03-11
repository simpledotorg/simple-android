package org.simple.clinic.summary

import com.f2prateek.rx.preferences2.Preference
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.Single
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import io.reactivex.rxkotlin.zipWith
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.analytics.Analytics
import org.simple.clinic.bp.BloodPressureRepository
import org.simple.clinic.drugs.PrescriptionRepository
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
import org.simple.clinic.overdue.Appointment.Status.CANCELLED
import org.simple.clinic.overdue.AppointmentCancelReason.InvalidPhoneNumber
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.PatientSummaryResult
import org.simple.clinic.patient.PatientSummaryResult.Saved
import org.simple.clinic.patient.PatientSummaryResult.Scheduled
import org.simple.clinic.summary.PatientSummaryCaller.NEW_PATIENT
import org.simple.clinic.summary.PatientSummaryCaller.SEARCH
import org.simple.clinic.util.Just
import org.simple.clinic.util.None
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.exhaustive
import org.simple.clinic.util.filterAndUnwrapJust
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named

typealias Ui = PatientSummaryScreen
typealias UiChange = (Ui) -> Unit

class PatientSummaryScreenController @Inject constructor(
    private val patientRepository: PatientRepository,
    private val bpRepository: BloodPressureRepository,
    private val prescriptionRepository: PrescriptionRepository,
    private val medicalHistoryRepository: MedicalHistoryRepository,
    private val appointmentRepository: AppointmentRepository,
    private val timestampGenerator: RelativeTimestampGenerator,
    private val utcClock: UtcClock,
    private val zoneId: ZoneId,
    private val configProvider: Single<PatientSummaryConfig>,
    @Named("patient_summary_result") private val patientSummaryResult: Preference<PatientSummaryResult>,
    @Named("time_for_bps_recorded") private val timeFormatterForBp: DateTimeFormatter
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .compose(mergeWithPatientSummaryChanges())
        .compose(mergeWithAllBloodPressuresDeleted())
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
        patientSummaryResultChanged(replayedEvents),
        showUpdatePhoneDialogIfRequired(replayedEvents),
        showScheduleAppointmentSheet(replayedEvents),
        goBackWhenBackClicked(replayedEvents),
        goToHomeOnDoneClick(replayedEvents))
  }

  private fun reportViewedPatientEvent(events: Observable<UiEvent>): Observable<UiChange> {
    return events.ofType<PatientSummaryScreenCreated>()
        .take(1L)
        .doOnNext { (patientUuid, caller) -> Analytics.reportViewedPatient(patientUuid, caller.name) }
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

    return Observables.combineLatest(sharedPatients, addresses, phoneNumbers)
        .map { (patient, address, phoneNumber) -> { ui: Ui -> ui.populatePatientProfile(patient, address, phoneNumber) } }
  }

  private fun mergeWithPatientSummaryChanges(): ObservableTransformer<UiEvent, UiEvent> {
    return ObservableTransformer { events ->
      val patientUuids = events
          .ofType<PatientSummaryScreenCreated>()
          .map { it.patientUuid }
          .distinctUntilChanged()

      val prescriptionItems = patientUuids
          .flatMap { prescriptionRepository.newestPrescriptionsForPatient(it) }
          .map(::SummaryPrescribedDrugsItem)

      val bloodPressures = Observables.combineLatest(patientUuids, configProvider.toObservable())
          .flatMap { (patientUuid, configProvider) -> bpRepository.newestMeasurementsForPatient(patientUuid, configProvider.numberOfBpsToDisplay) }
          .replay(1)
          .refCount()

      val displayTime = { instant: Instant ->
        instant.atZone(zoneId).format(timeFormatterForBp)
      }

      val bloodPressureItems = bloodPressures
          .map { bps ->
            val measurementsByDate = bps.groupBy { item -> item.createdAt.atZone(utcClock.zone).toLocalDate() }
            measurementsByDate.mapValues { (_, measurementList) ->
              measurementList.map { measurement ->
                val timestamp = timestampGenerator.generate(measurement.createdAt)
                SummaryBloodPressureListItem(
                    measurement = measurement,
                    daysAgo = timestamp,
                    showDivider = measurement == measurementList.last(),
                    formattedTime = if (measurementList.size > 1) displayTime(measurement.createdAt) else null,
                    addTopPadding = measurement == measurementList.first()
                )
              }
            }
          }
          .map { it.values.flatten() }

      val medicalHistoryItems = patientUuids
          .flatMap { medicalHistoryRepository.historyForPatientOrDefault(it) }
          .map { history ->
            val lastSyncTimestamp = timestampGenerator.generate(history.updatedAt)
            SummaryMedicalHistoryItem(history, lastSyncTimestamp)
          }

      // combineLatest() is important here so that the first data-set for the list
      // is dispatched in one go instead of them appearing one after another on the UI.
      val summaryItemChanges = Observables
          .combineLatest(
              prescriptionItems,
              bloodPressures,
              bloodPressureItems,
              medicalHistoryItems) { prescriptions, _, bpSummary, history ->
            PatientSummaryItemChanged(PatientSummaryItems(
                prescriptionItems = prescriptions,
                bloodPressureListItems = bpSummary,
                medicalHistoryItems = history
            ))
          }
          .distinctUntilChanged()

      events.mergeWith(summaryItemChanges)
    }
  }

  private fun mergeWithAllBloodPressuresDeleted(): ObservableTransformer<UiEvent, UiEvent> {
    return ObservableTransformer { events ->
      val allBloodPressuresDeleted = events
          .ofType<PatientSummaryScreenCreated>()
          .map { it.patientUuid }
          .switchMap(bpRepository::bloodPressureCount)
          .map { recordedBpCount -> PatientSummaryAllBloodPressuresDeleted(recordedBpCount == 0) }

      events.mergeWith(allBloodPressuresDeleted)
    }
  }

  private fun populateList(events: Observable<UiEvent>): Observable<UiChange> {
    val bloodPressurePlaceholders = events.ofType<PatientSummaryItemChanged>()
        .map { it ->
          val bpList = it.patientSummaryItems.bloodPressureListItems
          bpList.groupBy { item -> item.measurement.createdAt.atZone(utcClock.zone).toLocalDate() }
        }
        .map { it.size }
        .withLatestFrom(configProvider.toObservable())
        .map { (numberOfBloodPressures, config) ->
          val numberOfPlaceholders = 0.coerceAtLeast(config.numberOfBpPlaceholders - numberOfBloodPressures)

          (1..numberOfPlaceholders).map { placeholderNumber ->
            val shouldShowHint = numberOfBloodPressures == 0 && placeholderNumber == 1
            SummaryBloodPressurePlaceholderListItem(placeholderNumber, shouldShowHint)
          }
        }

    val patientSummaryListItem = events.ofType<PatientSummaryItemChanged>()
        .map { it.patientSummaryItems }

    return Observables.combineLatest(
        patientSummaryListItem,
        bloodPressurePlaceholders) { patientSummary, placeHolders ->
      { ui: Ui ->
        ui.populateList(patientSummary.prescriptionItems, placeHolders, patientSummary.bloodPressureListItems, patientSummary.medicalHistoryItems)
      }
    }
  }

  private fun updateMedicalHistory(events: Observable<UiEvent>): Observable<UiChange> {
    val patientUuids = events
        .ofType<PatientSummaryScreenCreated>()
        .map { it.patientUuid }

    val medicalHistories = patientUuids
        .flatMap { medicalHistoryRepository.historyForPatientOrDefault(it) }

    val updateHistory = { medicalHistory: MedicalHistory, question: MedicalHistoryQuestion, answer: MedicalHistory.Answer ->
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

    val autoShows = events
        .ofType<PatientSummaryScreenCreated>()
        .filter { it.caller == NEW_PATIENT }
        .withLatestFrom(patientUuid)
        .map { (_, patientUuid) -> { ui: Ui -> ui.showBloodPressureEntrySheetIfNotShownAlready(patientUuid) } }

    val newBpClicks = events
        .ofType<PatientSummaryNewBpClicked>()
        .withLatestFrom(patientUuid)
        .map { (_, patientUuid) -> { ui: Ui -> ui.showBloodPressureEntrySheet(patientUuid) } }

    return autoShows.mergeWith(newBpClicks)
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
    val patientUuids = events
        .ofType<PatientSummaryScreenCreated>()
        .map { it.patientUuid }

    val screenOpenedAt = events
        .ofType<PatientSummaryScreenCreated>()
        .map { it.screenCreatedTimestamp }

    val patientSummaryItemChanges = events
        .ofType<PatientSummaryItemChanged>()
        .map { it.patientSummaryItems }

    val hasSummaryItemChangedStream = Observables
        .combineLatest(screenOpenedAt, patientSummaryItemChanges)
        .map { (screenOpenedAt, patientSummaryItem) -> patientSummaryItem.hasItemChangedSince(screenOpenedAt) }

    val allBpsForPatientDeletedStream = events
        .ofType<PatientSummaryAllBloodPressuresDeleted>()
        .map { it.allBloodPressuresDeleted }

    val shouldShowScheduleAppointmentSheetOnBackClicksStream = Observables
        .combineLatest(hasSummaryItemChangedStream, allBpsForPatientDeletedStream)
        .map { (hasSummaryItemChanged, allBpsForPatientDeleted) ->
          if (allBpsForPatientDeleted) false else hasSummaryItemChanged
        }

    val showScheduleAppointmentSheetOnBackClicks = events
        .ofType<PatientSummaryBackClicked>()
        .withLatestFrom(shouldShowScheduleAppointmentSheetOnBackClicksStream, patientUuids)
        .filter { (_, shouldShowScheduleAppointmentSheet, _) -> shouldShowScheduleAppointmentSheet }
        .map { (_, _, uuid) -> { ui: Ui -> ui.showScheduleAppointmentSheet(patientUuid = uuid) } }

    val showScheduleAppointmentSheetOnDoneClicks = events
        .ofType<PatientSummaryDoneClicked>()
        .withLatestFrom(allBpsForPatientDeletedStream, patientUuids)
        .filter { (_, allBpsForPatientDeleted, _) -> allBpsForPatientDeleted.not() }
        .map { (_, _, uuid) -> { ui: Ui -> ui.showScheduleAppointmentSheet(patientUuid = uuid) } }

    return showScheduleAppointmentSheetOnBackClicks
        .mergeWith(showScheduleAppointmentSheetOnDoneClicks)
  }

  private fun goBackWhenBackClicked(events: Observable<UiEvent>): Observable<UiChange> {
    val callers = events
        .ofType<PatientSummaryScreenCreated>()
        .map { it.caller }

    val patientSummaryItemChanges = events
        .ofType<PatientSummaryItemChanged>()
        .map { it.patientSummaryItems }

    val screenOpenedAt = events
        .ofType<PatientSummaryScreenCreated>()
        .map { it.screenCreatedTimestamp }

    val hasSummaryItemChangedStream = Observables
        .combineLatest(screenOpenedAt, patientSummaryItemChanges)
        .map { (screenOpenedAt, patientSummaryItem) -> patientSummaryItem.hasItemChangedSince(screenOpenedAt) }

    val allBpsForPatientDeletedStream = events
        .ofType<PatientSummaryAllBloodPressuresDeleted>()
        .map { it.allBloodPressuresDeleted }

    val shouldGoBackStream = Observables
        .combineLatest(hasSummaryItemChangedStream, allBpsForPatientDeletedStream)
        .map { (hasSummaryItemChanged, allBpsForPatientDeleted) ->
          if (allBpsForPatientDeleted) true else hasSummaryItemChanged.not()
        }

    val backClicks = events
        .ofType<PatientSummaryBackClicked>()

    val goBackToHomeScreen = backClicks
        .withLatestFrom(shouldGoBackStream, callers)
        .filter { (_, shouldGoBack, _) -> shouldGoBack }
        .filter { (_, _, caller) -> caller == NEW_PATIENT }
        .map { { ui: Ui -> ui.goBackToHome() } }

    val goBackToSearchResults = backClicks
        .withLatestFrom(shouldGoBackStream, callers)
        .filter { (_, shouldGoBack, _) -> shouldGoBack }
        .filter { (_, _, caller) -> caller == SEARCH }
        .map { { ui: Ui -> ui.goBackToPatientSearch() } }

    return goBackToHomeScreen.mergeWith(goBackToSearchResults)
  }

  private fun goToHomeOnDoneClick(events: Observable<UiEvent>): Observable<UiChange> {
    val allBpsForPatientDeletedStream = events
        .ofType<PatientSummaryAllBloodPressuresDeleted>()
        .map { it.allBloodPressuresDeleted }

    return events
        .ofType<PatientSummaryDoneClicked>()
        .withLatestFrom(allBpsForPatientDeletedStream)
        .filter { (_, allBpsForPatientDeleted) -> allBpsForPatientDeleted }
        .map { { ui: Ui -> ui.goBackToHome() } }
  }

  private fun exitScreenAfterSchedulingAppointment(events: Observable<UiEvent>): Observable<UiChange> {
    val callers = events
        .ofType<PatientSummaryScreenCreated>()
        .map { it.caller }

    val scheduleAppointmentCloses = events
        .ofType<ScheduleAppointmentSheetClosed>()

    val backClicks = events
        .ofType<PatientSummaryBackClicked>()

    val doneClicks = events
        .ofType<PatientSummaryDoneClicked>()

    val afterBackClicks = scheduleAppointmentCloses
        .withLatestFrom(backClicks, callers)
        .map { (_, _, caller) ->
          { ui: Ui ->
            when (caller!!) {
              SEARCH -> ui.goBackToPatientSearch()
              NEW_PATIENT -> ui.goBackToHome()
            }.exhaustive()
          }
        }

    val afterDoneClicks = scheduleAppointmentCloses
        .withLatestFrom(doneClicks)
        .map { { ui: Ui -> ui.goBackToHome() } }

    return afterBackClicks.mergeWith(afterDoneClicks)
  }

  private fun openBloodPressureUpdateSheet(events: Observable<UiEvent>): Observable<UiChange> {
    return events.ofType<PatientSummaryBpClicked>()
        .map { it.bloodPressureMeasurement }
        .map { bp -> { ui: Ui -> ui.showBloodPressureUpdateSheet(bp.uuid) } }
  }

  private fun patientSummaryResultChanged(events: Observable<UiEvent>): Observable<UiChange> {
    val screenCreates = events
        .ofType<PatientSummaryScreenCreated>()

    val isPatientNew = screenCreates
        .filter { it.caller == NEW_PATIENT }
        .map { Saved(it.patientUuid) as PatientSummaryResult }

    val appointmentScheduled = events.ofType<AppointmentScheduled>()
        .withLatestFrom(screenCreates)
        .map { (_, createdEvent) -> Scheduled(createdEvent.patientUuid) as PatientSummaryResult }

    val wasPatientSummaryItemsChanged = events.ofType<PatientSummaryItemChanged>()
        .map { it.patientSummaryItems }
        .withLatestFrom(screenCreates)
        .filter { (item, createdEvent) -> item.hasItemChangedSince(createdEvent.screenCreatedTimestamp) }
        .map { (_, createdEvent) -> Saved(createdEvent.patientUuid) as PatientSummaryResult }

    return Observable.merge(isPatientNew, wasPatientSummaryItemsChanged, appointmentScheduled)
        .flatMap {
          patientSummaryResult.set(it)
          Observable.never<UiChange>()
        }
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

    val showForMissingPhone = screenCreations
        .filter { it.caller == SEARCH }
        .map { it.patientUuid }
        .switchMap { patientUuid ->
          isMissingPhoneAndShouldBeReminded(patientUuid)
              .take(1)
              .filter { missing -> missing }
              .map { { ui: Ui -> ui.showAddPhoneDialog(patientUuid) } }
        }

    return showForInvalidPhone.mergeWith(showForMissingPhone)
  }

  private fun hasInvalidPhone(patientUuid: UUID): Observable<Boolean> {
    return patientRepository.phoneNumber(patientUuid)
        .filterAndUnwrapJust()
        .zipWith(lastCancelledAppointment(patientUuid))
        .map { (number, appointment) -> appointment.updatedAt > number.updatedAt }
  }

  private fun isMissingPhoneAndShouldBeReminded(patientUuid: UUID): Observable<Boolean> {
    return patientRepository
        .phoneNumber(patientUuid)
        .zipWith(isFirstVisitByPatient(patientUuid))
        .map { (number, firstVisit) -> number is None && firstVisit }
  }

  private fun lastCancelledAppointment(patientUuid: UUID): Observable<Appointment> {
    return appointmentRepository
        .lastCreatedAppointmentForPatient(patientUuid)
        .filterAndUnwrapJust()
        .filter { it.status == CANCELLED && it.cancelReason == InvalidPhoneNumber }
  }

  /**
   * FYI: A patient is considered as "visited" when a BP is recorded.
   */
  private fun isFirstVisitByPatient(patientUuid: UUID): Observable<Boolean> {
    return bpRepository
        .bloodPressureCount(patientUuid)
        .buffer(2, 1)
        .filter { it.size == 2 }
        .take(1)
        .map { (firstCount, secondCount) -> firstCount == 0 && secondCount == 1 }
  }
}
