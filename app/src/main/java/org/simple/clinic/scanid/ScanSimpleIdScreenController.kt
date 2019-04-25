package org.simple.clinic.scanid

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.home.patients.PatientsScreenBpPassportCodeScanned
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.util.None
import org.simple.clinic.util.filterAndUnwrapJust
import org.simple.clinic.widgets.ScreenCreated
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

    return handleScannedBpPassportCodes(replayedEvents)
  }

  private fun mergeWithScannedBpPassportCodes() = ObservableTransformer<UiEvent, UiEvent> { events ->

    val scannedBpPassportCodes = events
        .ofType<ScanSimpleIdScreenQrCodeScanned>()
        .map { it.text }
        .map { scannedQrCode ->
          try {
            val bpPassportCode = UUID.fromString(scannedQrCode)
            ScanSimpleIdScreenPassportCodeScanned.ValidPassportCode(bpPassportCode)
          } catch (e: IllegalArgumentException) {
            ScanSimpleIdScreenPassportCodeScanned.InvalidPassportCode
          }
        }

    events.mergeWith(scannedBpPassportCodes)
  }

  private fun handleScannedBpPassportCodes(events: Observable<UiEvent>): Observable<UiChange> {
    val scannedBpPassportCodeStream = events
        .ofType<ScanSimpleIdScreenPassportCodeScanned.ValidPassportCode>()
        .take(1)

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
        .map { bpPassportCode -> Identifier(value = bpPassportCode.toString(), type = Identifier.IdentifierType.BpPassport) }
        .map { identifier -> { ui: Ui -> ui.openAddIdToPatientScreen(identifier) } }

    return openPatientSummary.mergeWith(openAddIdToPatientSearchScreen)
  }
}
