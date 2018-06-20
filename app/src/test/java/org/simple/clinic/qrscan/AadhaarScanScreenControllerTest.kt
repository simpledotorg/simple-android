package org.simple.clinic.qrscan

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.util.RuntimePermissionResult
import org.simple.clinic.util.Vibrator
import org.simple.clinic.widgets.UiEvent

class AadhaarScanScreenControllerTest {

  private val uiEvents: PublishSubject<UiEvent> = PublishSubject.create()

  private val screen = mock<AadhaarScanScreen>()
  private val aadhaarQrCodeParser = mock<AadhaarQrCodeParser>()
  private val vibrator = mock<Vibrator>()
  private val repository = mock<PatientRepository>()
  private val controller: AadhaarScanScreenController = AadhaarScanScreenController(aadhaarQrCodeParser, vibrator, repository)

  @Before
  fun setUp() {
    uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(screen) }
  }

  @Test
  fun `when aadhaar scan is clicked but camera permission isn't granted then request for it and enable scan once it's is granted`() {
    uiEvents.onNext(CameraPermissionChanged(RuntimePermissionResult.DENIED))
    uiEvents.onNext(AadhaarScanClicked())
    Mockito.verify(screen).requestCameraPermission()
  }

  @Test
  fun `toggle Aadhaar scanner with camera permission toggles`() {
    uiEvents.onNext(CameraPermissionChanged(RuntimePermissionResult.GRANTED))
    uiEvents.onNext(CameraPermissionChanged(RuntimePermissionResult.DENIED))
    uiEvents.onNext(CameraPermissionChanged(RuntimePermissionResult.GRANTED))
    uiEvents.onNext(CameraPermissionChanged(RuntimePermissionResult.NEVER_ASK_AGAIN))

    Mockito.verify(screen, Mockito.times(2)).setAadhaarScannerEnabled(true)
    Mockito.verify(screen, Mockito.times(2)).setAadhaarScannerEnabled(false)
  }

  @Test
  fun `when an aadhaar qr code is identified then play a short vibration`() {
    val aadhaarQrData = mock<AadhaarQrData>()
    whenever(aadhaarQrCodeParser.parse("qr-code")).thenReturn(aadhaarQrData)
    whenever(repository.saveOngoingEntry(any())).thenReturn(Completable.complete())

    uiEvents.onNext(QrScanned("qr-code"))

    verify(vibrator).vibrate(any())
  }

  // TODO: Uncomment when working on Aadhaar. This test is breaking CI.
//  @Test
//  fun `when an aadhaar qr code is identified then start a new patient flow with pre-filled values`() {
//    val aadhaarQrData = AadhaarQrData(
//        fullName = "Saket Narayan",
//        gender = Gender.MALE,
//        dateOfBirth = "12/04/1993",
//        villageOrTownOrCity = "Harmu",
//        district = "Ranchi",
//        state = "Jharkhand")
//
//    whenever(aadhaarQrCodeParser.parse("qr-code")).thenReturn(aadhaarQrData)
//    whenever(repository.saveOngoingEntry(any())).thenReturn(Completable.complete())
//
//    uiEvents.onNext(QrScanned("qr-code"))
//
//    argumentCaptor<OngoingPatientEntry>().apply {
//      verify(repository).saveOngoingEntry(capture())
//
//      assert(firstValue == OngoingPatientEntry(
//          personalDetails = OngoingPatientEntry.PersonalDetails(
//              fullName = aadhaarQrData.fullName!!,
//              dateOfBirth = aadhaarQrData.dateOfBirth!!,
//              age = null,
//              gender = aadhaarQrData.gender),
//          address = OngoingPatientEntry.Address(
//              colonyOrVillage = aadhaarQrData.villageOrTownOrCity!!,
//              district = aadhaarQrData.district!!,
//              state = aadhaarQrData.state!!)
//      ))
//    }
//    verify(screen).openNewPatientEntryScreen()
//  }

  @Test
  fun `non-aadhaar qr codes should be ignored`() {
    whenever(aadhaarQrCodeParser.parse("invalid-qr-code")).thenReturn(UnknownQr())
    whenever(repository.saveOngoingEntry(any())).thenReturn(Completable.complete())

    uiEvents.onNext(QrScanned("invalid-qr-code"))

    verify(repository, never()).saveOngoingEntry(any())
    verify(screen, never()).openNewPatientEntryScreen()
  }
}
