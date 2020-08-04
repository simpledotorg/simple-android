package org.simple.clinic.scanid

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import org.junit.After
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.BpPassport
import org.simple.clinic.scanid.ScanSimpleIdScreenPassportCodeScanned.InvalidPassportCode
import org.simple.clinic.scanid.ScanSimpleIdScreenPassportCodeScanned.ValidPassportCode
import org.simple.clinic.scanid.ShortCodeValidationResult.Failure.Empty
import org.simple.clinic.scanid.ShortCodeValidationResult.Failure.NotEqualToRequiredLength
import org.simple.clinic.util.Optional
import org.simple.clinic.util.toOptional
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

class ScanSimpleIdScreenControllerTest {

  private val uiEvents = PublishSubject.create<UiEvent>()
  private val screen = mock<ScanSimpleIdScreen>()
  private val patientRepository = mock<PatientRepository>()

  private lateinit var controllerSubscription: Disposable

  @After
  fun tearDown() {
    controllerSubscription.dispose()
  }

  @Test
  fun `when bp passport qr code is scanned and is assigned to the patient then open patient summary`() {
    val scannedCode = UUID.fromString("96d93a33-db68-4435-8c2b-372994a6a325")
    val patientUuid = UUID.fromString("2cdd0e63-0896-48ca-b066-364da2b27337")
    val patient = TestData.patient(uuid = patientUuid).toOptional()

    whenever(patientRepository.findPatientWithBusinessId(scannedCode.toString())).thenReturn(Observable.just(patient))

    setupController()
    uiEvents.onNext(ValidPassportCode(scannedCode))

    verify(screen).openPatientSummary(patientUuid)
  }

  @Test
  fun `when bp passport qr code is scanned and it is not assigned to patient then show add id to patient screen`() {
    // given
    val scannedCode = UUID.fromString("96d93a33-db68-4435-8c2b-372994a6a325")

    whenever(patientRepository.findPatientWithBusinessId(scannedCode.toString())) doReturn Observable.just(Optional.empty())

    // when
    setupController()
    uiEvents.onNext(ValidPassportCode(scannedCode))

    // then
    val identifier = Identifier(value = scannedCode.toString(), type = BpPassport)
    verify(screen).openAddIdToPatientScreen(identifier)
  }

  @Test
  fun `if scanned qr code is not a valid uuid then do nothing`() {
    val scannedCode = "96d93a33-db68"

    setupController()
    uiEvents.onNext(ScanSimpleIdScreenQrCodeScanned(scannedCode))

    verifyZeroInteractions(screen)
  }

  @Test
  fun `when the keyboard is up, then hide the QR code scanner view`() {
    // when
    setupController()
    uiEvents.onNext(ShowKeyboard)

    // then
    verify(screen).hideQrCodeScannerView()
    verifyNoMoreInteractions(screen)
  }

  @Test
  fun `when the keyboard is dismissed, then show the QR code scanner view`() {
    // when
    setupController()
    uiEvents.onNext(HideKeyboard)

    // then
    verify(screen).showQrCodeScannerView()
    verifyNoMoreInteractions(screen)
  }

  @Test
  fun `when the keyboard is up, then don't process invalid QR code scan events`() {
    // when
    setupController()
    with(uiEvents) {
      onNext(ShowKeyboard)
      onNext(InvalidPassportCode)
    }

    // then
    verify(screen).hideQrCodeScannerView()
    verifyNoMoreInteractions(screen)
  }

  @Test
  fun `when invalid (less than required length) short code is entered then show validation error`() {
    //given
    val shortCodeText = "3456"
    val shortCodeInput = ShortCodeInput(shortCodeText)

    //when
    setupController()
    uiEvents.onNext(ShortCodeSearched(shortCodeInput))

    //then
    verify(screen).showShortCodeValidationError(NotEqualToRequiredLength)
    verifyNoMoreInteractions(screen)
  }

  @Test
  fun `when short code text changes, then hide validation error`() {
    //given
    val invalidShortCode = "3456"
    val invalidShortCodeInput = ShortCodeInput(invalidShortCode)

    //when
    setupController()
    uiEvents.onNext(ShortCodeSearched(invalidShortCodeInput))
    uiEvents.onNext(ShortCodeChanged)

    //then
    verify(screen).showShortCodeValidationError(NotEqualToRequiredLength)
    verify(screen).hideShortCodeValidationError()
    verifyNoMoreInteractions(screen)
  }

  @Test
  fun `when user searches with a valid short code, then take the user to the patient search screen`() {
    // given
    val validShortCode = "1234567"
    val validShortCodeInput = ShortCodeInput(validShortCode)

    // when
    setupController()
    uiEvents.onNext(ShortCodeSearched(validShortCodeInput))

    // then
    verify(screen).openPatientShortCodeSearch(validShortCode)
    verifyNoMoreInteractions(screen)
  }

  @Test
  fun `when short code is empty, then show empty error`() {
    //given
    val emptyShortCodeInput = ShortCodeInput("")

    //when
    setupController()
    uiEvents.onNext(ShortCodeSearched(emptyShortCodeInput))

    //then
    verify(screen).showShortCodeValidationError(Empty)
    verifyNoMoreInteractions(screen)
  }

  private fun setupController() {
    val controller = ScanSimpleIdScreenController(patientRepository)

    controllerSubscription = uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(screen) }
  }
}
