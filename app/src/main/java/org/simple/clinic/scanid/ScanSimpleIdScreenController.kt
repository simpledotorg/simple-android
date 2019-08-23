package org.simple.clinic.scanid

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.BpPassport
import org.simple.clinic.scanid.ScanSimpleIdScreenPassportCodeScanned.InvalidPassportCode
import org.simple.clinic.scanid.ScanSimpleIdScreenPassportCodeScanned.ValidPassportCode
import org.simple.clinic.scanid.ShortCodeValidationResult.Failure
import org.simple.clinic.scanid.ShortCodeValidationResult.Success
import org.simple.clinic.util.None
import org.simple.clinic.util.filterAndUnwrapJust
import org.simple.clinic.widgets.UiEvent
import java.util.UUID
import javax.inject.Inject

typealias Ui = ScanSimpleIdScreen
typealias UiChange = (Ui) -> Unit

class ScanSimpleIdScreenController @Inject constructor(
    private val patientRepository: PatientRepository
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .compose(mergeWithScannedBpPassportCodes())
        .compose(ReportAnalyticsEvents())
        .replay()

    return Observable.merge(
        handleScannedBpPassportCodes(replayedEvents),
        handleKeyboardEvents(replayedEvents),
        handleShortCodeSearch(replayedEvents)
    )
  }

  private fun mergeWithScannedBpPassportCodes() = ObservableTransformer<UiEvent, UiEvent> { events ->
    val scannedBpPassportCodes = events
        .ofType<ScanSimpleIdScreenQrCodeScanned>()
        .map { it.text }
        .map { scannedQrCode ->
          try {
            val bpPassportCode = UUID.fromString(scannedQrCode)
            ValidPassportCode(bpPassportCode)
          } catch (e: IllegalArgumentException) {
            InvalidPassportCode
          }
        }

    events.mergeWith(scannedBpPassportCodes)
  }

  private fun handleKeyboardEvents(events: Observable<UiEvent>): Observable<UiChange> {
    val showKeyboardEvents = events
        .ofType<ShowKeyboard>()

    val hideKeyboardEvents = events
        .ofType<HideKeyboard>()

    val hideQrCodeScanner = showKeyboardEvents
        .map { { ui: Ui -> ui.hideQrCodeScannerView() } }

    val showQrCodeScanner = hideKeyboardEvents
        .map { { ui: Ui -> ui.showQrCodeScannerView() } }

    return Observable.merge(
        hideQrCodeScanner,
        showQrCodeScanner
    )
  }

  private fun handleScannedBpPassportCodes(events: Observable<UiEvent>): Observable<UiChange> {
    val scannedBpPassportCodeStream = events
        .ofType<ValidPassportCode>()
        .share()

    val foundPatientStream = scannedBpPassportCodeStream
        .map { scannedCode -> scannedCode.bpPassportUuid }
        .flatMap { patientRepository.findPatientWithBusinessId(it.toString()) }
        .replay()
        .refCount()

    val openPatientSummary = foundPatientStream
        .filterAndUnwrapJust()
        .map { patient -> { ui: Ui -> ui.openPatientSummary(patient.uuid) } }

    val openAddIdToPatientSearchScreen = Observables
        .combineLatest(foundPatientStream, scannedBpPassportCodeStream)
        .filter { (foundPatient, _) -> foundPatient is None }
        .map { (_, scannedBpPassportCode) -> scannedBpPassportCode.bpPassportUuid }
        .distinctUntilChanged()
        .map { bpPassportCode -> Identifier(value = bpPassportCode.toString(), type = BpPassport) }
        .map { identifier -> { ui: Ui -> ui.openAddIdToPatientScreen(identifier) } }

    return Observable.merge(
        openPatientSummary,
        openAddIdToPatientSearchScreen
    )
  }

  private fun handleShortCodeSearch(events: Observable<UiEvent>): Observable<UiChange> {
    val hideValidationErrors = events
        .ofType<ShortCodeChanged>()
        .map { { ui: Ui -> ui.hideShortCodeValidationError() } }

    val shortCodeInputs = events
        .ofType<ShortCodeSearched>()
        .map { it.shortCode }
        .share()

    val shortCodeValidationResults = shortCodeInputs
        .map { it.validate() }

    val showValidationErrors = shortCodeValidationResults
        .filter { it is Failure }
        .map { { ui: Ui -> ui.showShortCodeValidationError(it) } }

    val openPatientSearchScreenChanges = shortCodeValidationResults
        .filter { it is Success }
        .withLatestFrom(shortCodeInputs) { _, shortCodeInput -> shortCodeInput.shortCodeText }
        .map { { ui: Ui -> ui.openPatientShortCodeSearch(it) } }

    return Observable.merge(
        hideValidationErrors,
        showValidationErrors,
        openPatientSearchScreenChanges
    )
  }
}
