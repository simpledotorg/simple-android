package org.resolvetosavelives.red.summary

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import org.resolvetosavelives.red.bp.BloodPressureRepository
import org.resolvetosavelives.red.patient.PatientRepository
import org.resolvetosavelives.red.summary.PatientSummaryCaller.NEW_PATIENT
import org.resolvetosavelives.red.summary.PatientSummaryCaller.SEARCH
import org.resolvetosavelives.red.util.Just
import org.resolvetosavelives.red.widgets.UiEvent
import javax.inject.Inject

typealias Ui = PatientSummaryScreen
typealias UiChange = (Ui) -> Unit

class PatientSummaryScreenController @Inject constructor(
    private val patientRepository: PatientRepository,
    private val bpRepository: BloodPressureRepository,
    private val timestampGenerator: RelativeTimestampGenerator
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = events.replay(1).refCount()

    return Observable.mergeArray(
        populatePatientProfile(replayedEvents),
        populateBloodPressureHistory(replayedEvents),
        openBloodPressureBottomSheet(replayedEvents),
        handleBackClicks(replayedEvents))
  }

  private fun populatePatientProfile(events: Observable<UiEvent>): Observable<UiChange> {
    val patientUuid = events
        .ofType<PatientSummaryScreenCreated>()
        .map { it.patientUuid }

    val sharedPatients = patientUuid
        .flatMap { patientRepository.patient(it) }
        .map {
          // We do not expect the patient to get
          // deleted while this screen is already open.
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
        .map { (patient, address, phoneNumber) -> { ui: Ui -> ui.populatePatientInfo(patient, address, phoneNumber) } }
  }

  private fun populateBloodPressureHistory(events: Observable<UiEvent>): Observable<UiChange> {
    val setup = events
        .ofType<PatientSummaryScreenCreated>()
        .map { { ui: Ui -> ui.setupSummaryList() } }

    val populate = events
        .ofType<PatientSummaryScreenCreated>()
        .map { it.patientUuid }
        .flatMap { bpRepository.recentMeasurementsForPatient(it) }
        .map { measurements ->
          measurements.mapIndexed{ index, measurement ->
            val timestamp = timestampGenerator.generate(measurement.updatedAt)
            val isFirstItem = index == 0
            SummaryBloodPressureItem(measurement, timestamp, isFirstItem)
          }
        }
        .map { { ui: Ui -> ui.populateSummaryList(it) } }

    return setup.mergeWith(populate)
  }

  private fun openBloodPressureBottomSheet(events: Observable<UiEvent>): Observable<UiChange> {
    val patientUuid = events
        .ofType<PatientSummaryScreenCreated>()
        .map { it.patientUuid }

    val screenStartFromNewPatient = events
        .ofType<PatientSummaryScreenCreated>()
        .filter { it.caller == NEW_PATIENT }

    val newBpClicks = events
        .ofType<PatientSummaryNewBpClicked>()

    return Observable.merge(screenStartFromNewPatient, newBpClicks)
        .withLatestFrom(patientUuid)
        .map { (_, patientUuid) -> { ui: Ui -> ui.showBloodPressureEntrySheet(patientUuid) } }
  }

  private fun handleBackClicks(events: Observable<UiEvent>): Observable<UiChange> {
    val screenCallers = events
        .ofType<PatientSummaryScreenCreated>()
        .map { it.caller }

    return events
        .ofType<PatientSummaryBackClicked>()
        .withLatestFrom(screenCallers)
        .map { (_, caller) ->
          when (caller!!) {
            SEARCH -> { ui: Ui -> ui.goBackToPatientSearch() }
            NEW_PATIENT -> { ui: Ui -> ui.goBackToHome() }
          }
        }
  }
}
