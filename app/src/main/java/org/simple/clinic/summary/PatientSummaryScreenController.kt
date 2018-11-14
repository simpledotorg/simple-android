package org.simple.clinic.summary

import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.withLatestFrom
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.analytics.Analytics
import org.simple.clinic.bp.BloodPressureMeasurement
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
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.NONE
import org.simple.clinic.medicalhistory.MedicalHistoryRepository
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.PatientSummaryResult
import org.simple.clinic.patient.PatientSummaryResult.Saved
import org.simple.clinic.patient.PatientSummaryResult.Scheduled
import org.simple.clinic.summary.PatientSummaryCaller.NEW_PATIENT
import org.simple.clinic.summary.PatientSummaryCaller.SEARCH
import org.simple.clinic.util.Just
import org.simple.clinic.util.exhaustive
import org.simple.clinic.util.unwrapJust
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.Clock
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import timber.log.Timber
import javax.inject.Inject

typealias Ui = PatientSummaryScreen
typealias UiChange = (Ui) -> Unit

class PatientSummaryScreenController @Inject constructor(
    private val patientRepository: PatientRepository,
    private val bpRepository: BloodPressureRepository,
    private val prescriptionRepository: PrescriptionRepository,
    private val medicalHistoryRepository: MedicalHistoryRepository,
    private val timestampGenerator: RelativeTimestampGenerator,
    private val clock: Clock,
    private val configProvider: Single<PatientSummaryConfig>
) : ObservableTransformer<UiEvent, UiChange> {

  private val disposables = CompositeDisposable()

  /**
   * We do not want the UI stream to end if the count of subscribers change
   * midway while the merge() inside apply is going through all Ui changes.
   * As a solution, we're going to use autoConnect(), but that also means
   * that this Transformer's stream have to be disposed manually by the screen.
   */
  fun disposeOnDetach(ui: Ui) {
    RxView.detaches(ui)
        .take(1)
        .subscribe {
          disposables.clear()
        }
  }

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = events.compose(ReportAnalyticsEvents())
        .replay()
        .autoConnect(1) { d -> disposables += d }

    val dataset = constructListDataSet(replayedEvents)
        .replay()
        .autoConnect(1) { d -> disposables += d }

    val transformedEvents = Observable.merge(replayedEvents, dataset)

    val changes = patientSummaryResultChanged(transformedEvents)
        .replay()
        .autoConnect(1) { d -> disposables += d }

    val againTransformedEvents = Observable.merge(transformedEvents, changes)

    return Observable.mergeArray(
        populateList(againTransformedEvents),
        togglePatientEditFeature(againTransformedEvents),
        reportViewedPatientEvent(againTransformedEvents),
        populatePatientProfile(againTransformedEvents),
        updateMedicalHistory(againTransformedEvents),
        openBloodPressureBottomSheet(againTransformedEvents),
        openPrescribedDrugsScreen(againTransformedEvents),
        handleBackAndDoneClicks(againTransformedEvents),
        exitScreenAfterSchedulingAppointment(againTransformedEvents),
        openBloodPressureUpdateSheet(againTransformedEvents))
  }

  private fun togglePatientEditFeature(events: Observable<UiEvent>): Observable<UiChange> {
    return events.ofType<PatientSummaryScreenCreated>()
        .withLatestFrom(configProvider.toObservable()) { _, config -> config }
        .map { it.isPatientEditFeatureEnabled }
        .map { isPatientEditFeatureEnabled ->
          { ui: Ui ->
            if (isPatientEditFeatureEnabled) {
              ui.enableEditPatientFeature()
            } else {
              ui.disableEditPatientFeature()
            }
          }
        }
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
        .flatMap { patientRepository.phoneNumbers(it) }

    return Observables.combineLatest(sharedPatients, addresses, phoneNumbers)
        .map { (patient, address, phoneNumber) -> { ui: Ui -> ui.populatePatientProfile(patient, address, phoneNumber) } }
  }

  private fun constructListDataSet(events: Observable<UiEvent>): Observable<PatientSummaryItemChanged> {
    val patientUuids = events
        .ofType<PatientSummaryScreenCreated>()
        .map { it.patientUuid }
        .distinctUntilChanged()

    val prescriptionItems = patientUuids
        .flatMap { prescriptionRepository.newestPrescriptionsForPatient(it) }
        .map(::SummaryPrescribedDrugsItem)

    val bloodPressures = patientUuids
        .flatMap { bpRepository.newest100MeasurementsForPatient(it) }
        .replay(1)
        .refCount()

    val bloodPressureItems = bloodPressures
        .withLatestFrom(configProvider.toObservable()) { measurements, config -> measurements to config.bpEditableFor }
        .map { (measurements, bpEditableFor) ->
          measurements.map { measurement ->
            val timestamp = timestampGenerator.generate(measurement.updatedAt)
            SummaryBloodPressureListItem(measurement, timestamp, isEditable = isBpEditable(measurement, bpEditableFor))
          }
        }

    val medicalHistoryItems = patientUuids
        .flatMap { medicalHistoryRepository.historyForPatientOrDefault(it) }
        .map { history ->
          val lastSyncTimestamp = timestampGenerator.generate(history.updatedAt)
          SummaryMedicalHistoryItem(history, lastSyncTimestamp)
        }

    // combineLatest() is important here so that the first data-set for the list
    // is dispatched in one go instead of them appearing one after another on the UI.
    return Observables.combineLatest(
        prescriptionItems,
        bloodPressures,
        bloodPressureItems,
        medicalHistoryItems) { prescriptions, bp, bpSummary, history ->
      Timber.e("PatientSummaryItemChanged")
      PatientSummaryItemChanged(PatientSummaryItems(prescriptionItems = prescriptions, bloodPressures = bp, bloodPressureListItems = bpSummary, medicalHistoryItems = history))
    }
        .distinctUntilChanged()
  }

  private fun populateList(events: Observable<UiEvent>): Observable<UiChange> {

    val bloodPressurePlaceholders = events.ofType<PatientSummaryItemChanged>()
        .map { it.patientSummaryItem.bloodPressures.size }
        .withLatestFrom(configProvider.toObservable())
        .map { (numberOfBloodPressures, config) ->
          val numberOfPlaceholders = 0.coerceAtLeast(config.numberOfBpPlaceholders - numberOfBloodPressures)

          (1..numberOfPlaceholders).map { placeholderNumber ->
            val shouldShowHint = numberOfBloodPressures == 0 && placeholderNumber == 1
            SummaryBloodPressurePlaceholderListItem(placeholderNumber, shouldShowHint)
          }
        }

    val patientSummaryListItem = events.ofType<PatientSummaryItemChanged>()
        .map { it.patientSummaryItem }

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

    val updateHistory = { medicalHistory: MedicalHistory, question: MedicalHistoryQuestion, selected: Boolean ->
      when (question) {
        DIAGNOSED_WITH_HYPERTENSION -> medicalHistory.copy(diagnosedWithHypertension = selected)
        IS_ON_TREATMENT_FOR_HYPERTENSION -> medicalHistory.copy(isOnTreatmentForHypertension = selected)
        HAS_HAD_A_HEART_ATTACK -> medicalHistory.copy(hasHadHeartAttack = selected)
        HAS_HAD_A_STROKE -> medicalHistory.copy(hasHadStroke = selected)
        HAS_HAD_A_KIDNEY_DISEASE -> medicalHistory.copy(hasHadKidneyDisease = selected)
        HAS_DIABETES -> medicalHistory.copy(hasDiabetes = selected)
        NONE -> throw AssertionError("There's no none question in summary")
      }
    }

    return events.ofType<SummaryMedicalHistoryAnswerToggled>()
        .withLatestFrom(medicalHistories)
        .map { (toggleEvent, medicalHistory) ->
          updateHistory(medicalHistory, toggleEvent.question, toggleEvent.selected)
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
        .map { (_, patientUuid) -> { ui: Ui -> ui.showUpdatePrescribedDrugsScreen(patientUuid) } }
  }

  private fun handleBackAndDoneClicks(events: Observable<UiEvent>): Observable<UiChange> {
    val callers = events
        .ofType<PatientSummaryScreenCreated>()
        .map { it.caller }

    val patientUuids = events
        .ofType<PatientSummaryScreenCreated>()
        .map { it.patientUuid }

    val bloodPressureSaves = events
        .ofType<PatientSummaryBloodPressureClosed>()
        .startWith(PatientSummaryBloodPressureClosed(false))
        .map { it.wasBloodPressureSaved }

    val bloodPressureSaveRestores = events
        .ofType<PatientSummaryRestoredWithBPSaved>()
        .map { it.wasBloodPressureSaved }

    val mergedBpSaves = Observable.merge(bloodPressureSaves, bloodPressureSaveRestores)

    val patientSummaryResultItem = events.ofType<PatientSummaryResultSet>()
        .map { it.result }

    val backClicks = events
        .ofType<PatientSummaryBackClicked>()

    val doneClicks = events
        .ofType<PatientSummaryDoneClicked>()

    val doneOrBackClicksWithBpSaved = Observable.merge(doneClicks, backClicks)
        .withLatestFrom(mergedBpSaves, patientUuids)
        .filter { (_, saved, _) -> saved }
        .map { (_, _, uuid) -> { ui: Ui -> ui.showScheduleAppointmentSheet(patientUuid = uuid) } }

    val backClicksWithBpNotSaved = backClicks
        .withLatestFrom(mergedBpSaves, callers, patientSummaryResultItem) { _, saves , uuid , result -> Triple(saves, uuid, result) }
        .filter { (saved, _) -> saved.not() }
        .map { (_, caller, result) ->
          { ui: Ui ->
            when (caller!!) {
              SEARCH -> ui.goBackToPatientSearch()
              NEW_PATIENT -> ui.goBackToHome(result)
            }.exhaustive()
          }
        }

    val doneClicksWithBpNotSaved = doneClicks
        .withLatestFrom(mergedBpSaves, patientSummaryResultItem)
        .filter { (_, saved) -> saved.not() }
        .map { (_, _, result) -> result }
        .map { { ui: Ui -> ui.goBackToHome(it) } }

    return Observable.mergeArray(
        doneOrBackClicksWithBpSaved,
        backClicksWithBpNotSaved,
        doneClicksWithBpNotSaved)
  }

  private fun exitScreenAfterSchedulingAppointment(events: Observable<UiEvent>): Observable<UiChange> {
    val callers = events
        .ofType<PatientSummaryScreenCreated>()
        .map { it.caller }

    val patientSummaryResultItem = events.ofType<PatientSummaryResultSet>()
        .map { it.result }

    val scheduleAppointmentCloses = events
        .ofType<ScheduleAppointmentSheetClosed>()

    val backClicks = events
        .ofType<PatientSummaryBackClicked>()

    val doneClicks = events
        .ofType<PatientSummaryDoneClicked>()

    val afterBackClicks = scheduleAppointmentCloses
        .withLatestFrom(backClicks, callers, patientSummaryResultItem) { _,_, caller, result -> caller to result  }
        .map { (caller, result) ->
          { ui: Ui ->
            when (caller!!) {
              SEARCH -> ui.goBackToPatientSearch()
              NEW_PATIENT -> ui.goBackToHome(result)
            }.exhaustive()
          }
        }

    val afterDoneClicks = scheduleAppointmentCloses
        .withLatestFrom(patientSummaryResultItem, doneClicks)
        .map { (_, item, _) -> { ui: Ui -> ui.goBackToHome(item) } }

    return afterBackClicks.mergeWith(afterDoneClicks)
  }

  private fun openBloodPressureUpdateSheet(events: Observable<UiEvent>): Observable<UiChange> {
    return events.ofType<PatientSummaryBpClicked>()
        .map { it.bloodPressureMeasurement }
        .withLatestFrom(configProvider.toObservable())
        .filter { (bloodPressureMeasurement, config) -> isBpEditable(bloodPressureMeasurement, config.bpEditableFor) }
        .map { (bloodPressureMeasurement, _) ->
          { ui: Ui -> ui.showBloodPressureUpdateSheet(bloodPressureMeasurement.uuid) }
        }
  }

  private fun patientSummaryResultChanged(events: Observable<UiEvent>): Observable<PatientSummaryResultSet> {
    val patientUuid = events
        .ofType<PatientSummaryScreenCreated>()
        .map { it.patientUuid }

    val sharedPatients = patientUuid
        .flatMap { patientRepository.patient(it) }
        // We do not expect the patient to get deleted while this screen is already open.
        .unwrapJust()
        .replay(1)
        .refCount()

    val appointmentScheduled = events.ofType<AppointmentScheduled>()
        .map { it.appointmentDate }
        .withLatestFrom(sharedPatients)
        .map { (appointmentDate, patient) -> Scheduled(patient.fullName, appointmentDate) as PatientSummaryResult }

    val wasPatientSummaryItemsChanged = events.ofType<PatientSummaryItemChanged>()
        .map { Saved as PatientSummaryResult }
        .skip(1)
        .doOnNext { Timber.e("Distinct item") }

    return wasPatientSummaryItemsChanged.mergeWith(appointmentScheduled)
        .doOnNext { Timber.e("Result: $it") }
        .map { PatientSummaryResultSet(it) }
        .startWith(PatientSummaryResultSet(PatientSummaryResult.NotSaved))
  }

  private fun isBpEditable(bloodPressureMeasurement: BloodPressureMeasurement, bpEditableFor: Duration): Boolean {
    val now = Instant.now(clock)
    val editExpiresAt = bloodPressureMeasurement.createdAt.plus(bpEditableFor)

    return now <= editExpiresAt
  }
}
