package org.resolvetosavelives.red.qrscan

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.Single
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import org.resolvetosavelives.red.newentry.search.OngoingPatientEntry
import org.resolvetosavelives.red.newentry.search.PatientRepository
import org.resolvetosavelives.red.util.RuntimePermissionResult
import org.resolvetosavelives.red.util.Vibrator
import org.resolvetosavelives.red.widgets.ScreenCreated
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
        scannerSetups(replayedEvents),
        aadhaarScannerToggles(replayedEvents),
        cameraPermissionRequests(replayedEvents),
        appInfoOpens(replayedEvents),
        aadhaarScans(replayedEvents))
  }

  // TODO: Test.
  private fun scannerSetups(events: Observable<UiEvent>): Observable<UiChange>? {
    return events
        .ofType<ScreenCreated>()
        .map { { ui: Ui -> ui.setupQrScanner() } }
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

  private fun appInfoOpens(events: Observable<UiEvent>): Observable<UiChange> {
    val cameraPermissionChanges = events
        .ofType<CameraPermissionChanged>()
        .map(CameraPermissionChanged::result)

    return events
        .ofType<AadhaarScanClicked>()
        .withLatestFrom(cameraPermissionChanges)
        .filter({ (_, permissionResult) -> permissionResult == RuntimePermissionResult.NEVER_ASK_AGAIN })
        .flatMap { Observable.just { ui: Ui -> ui.openAppInfoToManuallyEnableCameraAccess() } }
  }

  private fun aadhaarScannerToggles(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<CameraPermissionChanged>()
        .map(CameraPermissionChanged::result)
        .map({ result -> result == RuntimePermissionResult.GRANTED })
        .map { hasPermission -> { ui: Ui -> ui.setAadhaarScannerEnabled(hasPermission) } }
  }

  // TODO: Test.
  private fun aadhaarScans(events: Observable<UiEvent>): Observable<UiChange> {
    val vibrations = events
        .ofType<QrScanned>()
        .flatMapCompletable { Completable.fromAction({ vibrator.vibrate(millis = 100) }) }
        .toObservable<UiChange>()

    val newPatientFlows = events
        .ofType<QrScanned>()
        .map({ event -> event.qrCode })
        .doOnNext({ qrCode -> Timber.i("qrCode: $qrCode") })
        .flatMapSingle { qrCode ->
          Single.just(aadhaarQrCodeParser.parse(qrCode))
              .doOnError({ e -> Timber.i("Couldn't parse aadhaar qr: $qrCode") })
        }
        .map { aadhaarData -> patientEntry(aadhaarData) }
        .take(1)
        .flatMapCompletable { newEntry -> repository.save(newEntry) }
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
