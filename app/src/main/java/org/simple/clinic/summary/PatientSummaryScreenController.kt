package org.simple.clinic.summary

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import org.simple.clinic.bp.BloodPressureRepository
import org.simple.clinic.drugs.PrescriptionRepository
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.summary.PatientSummaryCaller.NEW_PATIENT
import org.simple.clinic.summary.PatientSummaryCaller.SEARCH
import org.simple.clinic.util.Just
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

typealias Ui = PatientSummaryScreen
typealias UiChange = (Ui) -> Unit

class PatientSummaryScreenController @Inject constructor(
    private val patientRepository: PatientRepository,
    private val bpRepository: BloodPressureRepository,
    private val prescriptionRepository: PrescriptionRepository,
    private val timestampGenerator: RelativeTimestampGenerator
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = events.replay(1).refCount()

    val transformedEvents = replayedEvents
        .mergeWith(handleDoneClicks(replayedEvents))

    return Observable.mergeArray(
        populatePatientProfile(transformedEvents),
        constructPrescribedDrugsHistory(transformedEvents),
        constructBloodPressureHistory(transformedEvents),
        openBloodPressureBottomSheet(transformedEvents),
        openPrescribedDrugsScreen(transformedEvents),
        handleBackClicks(transformedEvents))
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

  private fun constructPrescribedDrugsHistory(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<PatientSummaryScreenCreated>()
        .map { it.patientUuid }
        .flatMap { prescriptionRepository.newestPrescriptionsForPatient(it) }
        .map(::SummaryPrescribedDrugsItem)
        .map { { ui: Ui -> ui.populatePrescribedDrugsSummary(it) } }
  }

  private fun constructBloodPressureHistory(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<PatientSummaryScreenCreated>()
        .map { it.patientUuid }
        .flatMap { bpRepository.newest100MeasurementsForPatient(it) }
        .map { measurements ->
          measurements.map { measurement ->
            val timestamp = timestampGenerator.generate(measurement.updatedAt)
            SummaryBloodPressureItem(measurement, timestamp)
          }
        }
        .map { { ui: Ui -> ui.populateBloodPressureHistory(it) } }
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

  private fun handleDoneClicks(events: Observable<UiEvent>): Observable<UiEvent> {
    return events
        .ofType<PatientSummaryDoneClicked>()
        .map { PatientSummaryBackClicked() }
  }
}
