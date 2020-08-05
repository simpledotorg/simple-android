package org.simple.clinic.scanid

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import com.spotify.mobius.Init
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import io.reactivex.subjects.PublishSubject
import org.junit.After
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.mobius.first
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.BpPassport
import org.simple.clinic.scanid.ShortCodeValidationResult.Failure.Empty
import org.simple.clinic.scanid.ShortCodeValidationResult.Failure.NotEqualToRequiredLength
import org.simple.clinic.util.Optional
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import org.simple.clinic.util.toOptional
import org.simple.clinic.widgets.UiEvent
import org.simple.mobius.migration.MobiusTestFixture
import java.util.UUID

class ScanSimpleIdScreenControllerTest {

  private val uiEvents = PublishSubject.create<UiEvent>()
  private val ui = mock<ScanSimpleIdUi>()
  private val uiActions = mock<ScanSimpleIdUiActions>()
  private val patientRepository = mock<PatientRepository>()

  private lateinit var testFixture: MobiusTestFixture<ScanSimpleIdModel, ScanSimpleIdEvent, ScanSimpleIdEffect>

  @After
  fun tearDown() {
    testFixture.dispose()
  }

  @Test
  fun `when bp passport qr code is scanned and is assigned to the patient then open patient summary`() {
    val scannedCode = UUID.fromString("96d93a33-db68-4435-8c2b-372994a6a325")
    val patientUuid = UUID.fromString("2cdd0e63-0896-48ca-b066-364da2b27337")
    val patient = TestData.patient(uuid = patientUuid).toOptional()

    whenever(patientRepository.findPatientWithBusinessId(scannedCode.toString())).thenReturn(Observable.just(patient))

    // when
    setupController()
    uiEvents.onNext(ScanSimpleIdScreenQrCodeScanned(scannedCode.toString()))

    // then
    verify(uiActions).openPatientSummary(patientUuid)
    verifyNoMoreInteractions(ui, uiActions)
  }

  @Test
  fun `when bp passport qr code is scanned and it is not assigned to patient then show add id to patient screen`() {
    // given
    val scannedCode = UUID.fromString("96d93a33-db68-4435-8c2b-372994a6a325")

    whenever(patientRepository.findPatientWithBusinessId(scannedCode.toString())) doReturn Observable.just(Optional.empty())

    // when
    setupController()
    uiEvents.onNext(ScanSimpleIdScreenQrCodeScanned(scannedCode.toString()))

    // then
    val identifier = Identifier(value = scannedCode.toString(), type = BpPassport)
    verify(uiActions).openAddIdToPatientScreen(identifier)
    verifyNoMoreInteractions(ui, uiActions)
  }

  @Test
  fun `if scanned qr code is not a valid uuid then do nothing`() {
    // given
    val scannedCode = "96d93a33-db68"

    // when
    setupController()
    uiEvents.onNext(ScanSimpleIdScreenQrCodeScanned(scannedCode))

    // then
    verifyZeroInteractions(ui, uiActions)
  }

  @Test
  fun `when the keyboard is up, then hide the QR code scanner view`() {
    // when
    setupController()
    uiEvents.onNext(ShowKeyboard)

    // then
    verify(uiActions).hideQrCodeScannerView()
    verifyNoMoreInteractions(ui, uiActions)
  }

  @Test
  fun `when the keyboard is dismissed, then show the QR code scanner view`() {
    // when
    setupController()
    uiEvents.onNext(HideKeyboard)

    // then
    verify(uiActions).showQrCodeScannerView()
    verifyNoMoreInteractions(ui, uiActions)
  }

  @Test
  fun `when the keyboard is up, then don't process invalid QR code scan events`() {
    // when
    setupController()
    with(uiEvents) {
      onNext(ShowKeyboard)
      onNext(ScanSimpleIdScreenQrCodeScanned("96d93a33-db68"))
    }

    // then
    verify(uiActions).hideQrCodeScannerView()
    verifyNoMoreInteractions(ui, uiActions)
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
    verify(uiActions).showShortCodeValidationError(NotEqualToRequiredLength)
    verifyNoMoreInteractions(ui, uiActions)
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
    verify(uiActions).showShortCodeValidationError(NotEqualToRequiredLength)
    verify(uiActions).hideShortCodeValidationError()
    verifyNoMoreInteractions(ui, uiActions)
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
    verify(uiActions).openPatientShortCodeSearch(validShortCode)
    verifyNoMoreInteractions(ui, uiActions)
  }

  @Test
  fun `when short code is empty, then show empty error`() {
    //given
    val emptyShortCodeInput = ShortCodeInput("")

    //when
    setupController()
    uiEvents.onNext(ShortCodeSearched(emptyShortCodeInput))

    //then
    verify(uiActions).showShortCodeValidationError(Empty)
    verifyNoMoreInteractions(ui, uiActions)
  }

  private fun setupController() {
    val effectHandler = ScanSimpleIdEffectHandler(
        patientRepository = patientRepository,
        schedulersProvider = TestSchedulersProvider.trampoline(),
        uiActions = uiActions
    )

    val uiRenderer = ScanSimpleIdUiRenderer(ui)

    testFixture = MobiusTestFixture(
        events = uiEvents.ofType(),
        init = Init { first(it) },
        update = ScanSimpleIdUpdate(),
        effectHandler = effectHandler.build(),
        defaultModel = ScanSimpleIdModel.create(),
        modelUpdateListener = uiRenderer::render
    )

    testFixture.start()
  }
}
