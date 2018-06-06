package org.resolvetosavelives.red.qrscan

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.Single
import io.reactivex.rxkotlin.cast
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import org.resolvetosavelives.red.search.OngoingPatientEntry
import org.resolvetosavelives.red.search.PatientRepository
import org.resolvetosavelives.red.util.RuntimePermissionResult
import org.resolvetosavelives.red.util.Vibrator
import org.resolvetosavelives.red.widgets.UiEvent
import timber.log.Timber
import javax.inject.Inject

private typealias Ui = AadhaarScanScreen
private typealias UiChange = (Ui) -> Unit

class AadhaarScanScreenController @Inject constructor(
    private val aadhaarQrCodeParser: AadhaarQrCodeParser,
    private val vibrator: Vibrator,
    private val repository: PatientRepository
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = events.replay().refCount()

    return Observable.mergeArray(
        aadhaarScannerToggles(replayedEvents),
        cameraPermissionRequests(replayedEvents),
        aadhaarScans(replayedEvents))
  }

  private fun cameraPermissionRequests(events: Observable<UiEvent>): Observable<UiChange> {
    val cameraPermissionChanges = events
        .ofType<CameraPermissionChanged>()
        .map(CameraPermissionChanged::result)

    return events
        .ofType<AadhaarScanClicked>()
        .withLatestFrom(cameraPermissionChanges)
        .filter({ (_, permissionResult) -> permissionResult == RuntimePermissionResult.DENIED })
        .flatMap { Observable.just { ui: Ui -> ui.requestCameraPermission() } }
  }

  private fun aadhaarScannerToggles(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<CameraPermissionChanged>()
        .map(CameraPermissionChanged::result)
        .map({ result -> result == RuntimePermissionResult.GRANTED })
        .map { hasPermission -> { ui: Ui -> ui.setAadhaarScannerEnabled(hasPermission) } }
  }

  private fun aadhaarScans(events: Observable<UiEvent>): Observable<UiChange> {
    val successfulAadhaarScans = events
        .ofType<QrScanned>()
        .map({ event -> event.qrCode })
        .flatMapSingle { qrCode ->
          Single.just(aadhaarQrCodeParser.parse(qrCode))
              .doOnError({ e -> Timber.e(e, "Couldn't parse aadhaar qr: $qrCode") })
        }
        .share()
        .filter({ result -> result is AadhaarQrData })
        .cast<AadhaarQrData>()

    val vibrations = successfulAadhaarScans
        .flatMapCompletable { Completable.fromAction({ vibrator.vibrate(millis = 100) }) }
        .toObservable<UiChange>()

    val newPatientFlows = successfulAadhaarScans
        .take(1)
        .map { aadhaarData -> patientEntry(aadhaarData) }
        .flatMapCompletable { newEntry -> repository.saveOngoingEntry(newEntry) }
        .andThen(Observable.just({ ui: Ui -> ui.openNewPatientEntryScreen() }))

    return vibrations.mergeWith(newPatientFlows)
  }

  private fun patientEntry(aadhaarQrData: AadhaarQrData): OngoingPatientEntry {
    return OngoingPatientEntry(
        personalDetails = OngoingPatientEntry.PersonalDetails(
            fullName = aadhaarQrData.fullName.orEmpty(),
            dateOfBirth = aadhaarQrData.dateOfBirth.orEmpty(),
            ageWhenCreated = null,
            gender = aadhaarQrData.gender),
        address = OngoingPatientEntry.Address(
            colonyOrVillage = aadhaarQrData.villageOrTownOrCity.orEmpty(),
            district = aadhaarQrData.district.orEmpty(),
            state = aadhaarQrData.state.orEmpty())
    )
  }
}
