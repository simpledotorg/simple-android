package org.simple.clinic.scanid

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.spotify.mobius.Init
import com.squareup.moshi.Moshi
import io.reactivex.rxkotlin.ofType
import io.reactivex.subjects.PublishSubject
import org.junit.After
import org.junit.Test
import org.simple.clinic.mobius.first
import org.simple.clinic.platform.crash.NoOpCrashReporter
import org.simple.clinic.scanid.EnteredCodeValidationResult.Failure.Empty
import org.simple.clinic.scanid.EnteredCodeValidationResult.Failure.NotEqualToRequiredLength
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import org.simple.clinic.widgets.UiEvent
import org.simple.mobius.migration.MobiusTestFixture

class ScanSimpleIdScreenLogicTest {

  private val uiEvents = PublishSubject.create<UiEvent>()
  private val uiActions = mock<ScanSimpleIdUiActions>()

  private lateinit var testFixture: MobiusTestFixture<ScanSimpleIdModel, ScanSimpleIdEvent, ScanSimpleIdEffect>

  @After
  fun tearDown() {
    testFixture.dispose()
  }

  @Test
  fun `if scanned qr code is not a valid uuid then do nothing`() {
    // given
    val scannedCode = "96d93a33-db68"

    // when
    setupController()
    uiEvents.onNext(ScanSimpleIdScreenQrCodeScanned(scannedCode))

    // then
    verifyZeroInteractions(uiActions)
  }

  @Test
  fun `when the keyboard is up, then hide the QR code scanner view`() {
    // when
    setupController()
    uiEvents.onNext(ShowKeyboard)

    // then
    verify(uiActions).hideQrCodeScannerView()
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when the keyboard is dismissed, then show the QR code scanner view`() {
    // when
    setupController()
    uiEvents.onNext(HideKeyboard)

    // then
    verify(uiActions).showQrCodeScannerView()
    verifyNoMoreInteractions(uiActions)
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
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when invalid (less than required length) short code is entered then show validation error`() {
    //given
    val shortCodeText = "3456"
    val shortCodeInput = EnteredCodeInput(shortCodeText)

    //when
    setupController()
    uiEvents.onNext(EnteredCodeSearched(shortCodeInput))

    //then
    verify(uiActions).showEnteredCodeValidationError(NotEqualToRequiredLength)
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when short code text changes, then hide validation error`() {
    //given
    val invalidShortCode = "3456"
    val invalidShortCodeInput = EnteredCodeInput(invalidShortCode)

    //when
    setupController()
    uiEvents.onNext(EnteredCodeSearched(invalidShortCodeInput))
    uiEvents.onNext(EnteredCodeChanged)

    //then
    verify(uiActions).showEnteredCodeValidationError(NotEqualToRequiredLength)
    verify(uiActions).hideEnteredCodeValidationError()
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when short code is empty, then show empty error`() {
    //given
    val emptyShortCodeInput = EnteredCodeInput("")

    //when
    setupController()
    uiEvents.onNext(EnteredCodeSearched(emptyShortCodeInput))

    //then
    verify(uiActions).showEnteredCodeValidationError(Empty)
    verifyNoMoreInteractions(uiActions)
  }

  private fun setupController() {
    val effectHandler = ScanSimpleIdEffectHandler(
        schedulersProvider = TestSchedulersProvider.trampoline(),
        patientRepository = mock(),
        uiActions = uiActions
    )

    testFixture = MobiusTestFixture(
        events = uiEvents.ofType(),
        init = Init { first(it) },
        update = ScanSimpleIdUpdate(crashReporter = NoOpCrashReporter(), Moshi.Builder().build()),
        effectHandler = effectHandler.build(),
        defaultModel = ScanSimpleIdModel.create(),
        modelUpdateListener = { /* no-op */ }
    )

    testFixture.start()
  }
}
