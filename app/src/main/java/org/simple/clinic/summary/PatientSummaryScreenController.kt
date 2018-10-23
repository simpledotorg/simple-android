package org.simple.clinic.summary

import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
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
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.NONE
import org.simple.clinic.medicalhistory.MedicalHistoryRepository
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.summary.PatientSummaryCaller.NEW_PATIENT
import org.simple.clinic.summary.PatientSummaryCaller.SEARCH
import org.simple.clinic.util.Just
import org.simple.clinic.util.exhaustive
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.Clock
import org.threeten.bp.Instant
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

  private lateinit var disposable: Disposable

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
          disposable.dispose()
        }
  }

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = events.compose(ReportAnalyticsEvents())
        .replay()
        .autoConnect(1) { d -> disposable = d }

    return Observable.mergeArray(
        reportViewedPatientEvent(replayedEvents),
        populatePatientProfile(replayedEvents),
        constructListDataSet(replayedEvents),
        updateMedicalHistory(replayedEvents),
        openBloodPressureBottomSheet(replayedEvents),
        openPrescribedDrugsScreen(replayedEvents),
        handleBackAndDoneClicks(replayedEvents),
        exitScreenAfterSchedulingAppointment(replayedEvents),
        openBloodPressureUpdateSheet(replayedEvents))
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

  private fun constructListDataSet(events: Observable<UiEvent>): Observable<UiChange> {
    val patientUuids = events
        .ofType<PatientSummaryScreenCreated>()
        .map { it.patientUuid }

    val prescriptionItems = patientUuids
        .flatMap { prescriptionRepository.newestPrescriptionsForPatient(it) }
        .map(::SummaryPrescribedDrugsItem)

    val bloodPressures = patientUuids
        .flatMap { bpRepository.newest100MeasurementsForPatient(it) }
        .replay(1)
        .refCount()

    val bloodPressureItems = bloodPressures
        .map { measurements ->
          measurements.map { measurement ->
            val timestamp = timestampGenerator.generate(measurement.updatedAt)
            SummaryBloodPressureListItem(measurement, timestamp)
          }
        }

    val bloodPressurePlaceholders = bloodPressures
        .map { it.size }
        .withLatestFrom(configProvider.toObservable())
        .map { (numberOfBloodPressures, config) ->
          val numberOfPlaceholders = 0.coerceAtLeast(config.numberOfBpPlaceholders - numberOfBloodPressures)

          (1..numberOfPlaceholders).map { placeholderNumber ->
            val shouldShowHint = numberOfBloodPressures == 0 && placeholderNumber == 1
            SummaryBloodPressurePlaceholderListItem(placeholderNumber, shouldShowHint)
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
        bloodPressureItems,
        medicalHistoryItems,
        bloodPressurePlaceholders) { prescriptions, bp, history, placeHolders ->
      { ui: Ui -> ui.populateList(prescriptions, placeHolders, bp, history) }
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

    val backClicks = events
        .ofType<PatientSummaryBackClicked>()

    val doneClicks = events
        .ofType<PatientSummaryDoneClicked>()

    val doneOrBackClicksWithBpSaved = Observable.merge(doneClicks, backClicks)
        .withLatestFrom(mergedBpSaves, patientUuids)
        .filter { (_, saved, _) -> saved }
        .map { (_, _, uuid) -> { ui: Ui -> ui.showScheduleAppointmentSheet(patientUuid = uuid) } }

    val backClicksWithBpNotSaved = backClicks
        .withLatestFrom(mergedBpSaves, callers)
        .filter { (_, saved, _) -> saved.not() }
        .map { (_, _, caller) ->
          { ui: Ui ->
            when (caller!!) {
              SEARCH -> ui.goBackToPatientSearch()
              NEW_PATIENT -> ui.goBackToHome()
            }.exhaustive()
          }
        }

    val doneClicksWithBpNotSaved = doneClicks
        .withLatestFrom(mergedBpSaves)
        .filter { (_, saved) -> saved.not() }
        .map { { ui: Ui -> ui.goBackToHome() } }

    return Observable.mergeArray(
        doneOrBackClicksWithBpSaved,
        backClicksWithBpNotSaved,
        doneClicksWithBpNotSaved)
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
        .withLatestFrom(configProvider.toObservable())
        .filter { (bloodPressureMeasurement, config) ->
          val now = Instant.now(clock)
          val cannotEditAfter = bloodPressureMeasurement.createdAt.plus(config.bpEditableFor)

          now.isBefore(cannotEditAfter) || now == cannotEditAfter
        }
        .map { (bloodPressureMeasurement, _) ->
          { ui: Ui -> ui.showBloodPressureUpdateSheet(bloodPressureMeasurement.uuid) }
        }
  }
}
