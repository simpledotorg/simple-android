package org.simple.clinic.scanid

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.BpPassport
import org.simple.clinic.scanid.ScanSimpleIdScreenPassportCodeScanned.InvalidPassportCode
import org.simple.clinic.scanid.ScanSimpleIdScreenPassportCodeScanned.ValidPassportCode
import org.simple.clinic.util.Just
import org.simple.clinic.util.None
import org.simple.clinic.util.Optional
import org.simple.clinic.util.toOptional
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

@RunWith(JUnitParamsRunner::class)
class ScanSimpleIdScreenControllerTest {

  private val uiEvents = PublishSubject.create<UiEvent>()
  private val screen = mock<ScanSimpleIdScreen>()
  private val patientRepository = mock<PatientRepository>()
  private val controller = ScanSimpleIdScreenController(patientRepository)

  @Before
  fun setUp() {
    uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(screen) }
  }

  @Test
  @Parameters(method = "params for scanning simple passport qr code")
  fun `when bp passport qr code is scanned and it is valid then appropriate screen should open`(
      validScannedCode: UUID,
      foundPatient: Optional<Patient>
  ) {
    whenever(patientRepository.findPatientWithBusinessId(validScannedCode.toString())).thenReturn(Observable.just(foundPatient))
    uiEvents.onNext(ValidPassportCode(validScannedCode))

    when (foundPatient) {
      is Just -> verify(screen).openPatientSummary(foundPatient.value.uuid)
      is None -> {
        val identifier = Identifier(value = validScannedCode.toString(), type = BpPassport)
        verify(screen).openAddIdToPatientScreen(identifier)
      }
    }
  }

  @Test
  @Parameters(method = "params for scanning valid uuids")
  fun `the qr code must be sent only if it is a valid uuid`(
      scannedTexts: List<String>,
      expectedScannedCode: UUID?
  ) {
    whenever(patientRepository.findPatientWithBusinessId(any())).thenReturn(Observable.just(None))

    scannedTexts.forEach { scannedText ->
      uiEvents.onNext(ScanSimpleIdScreenQrCodeScanned(scannedText))
    }

    if (expectedScannedCode == null) {
      verify(screen, never()).openPatientSummary(any())
      verify(screen, never()).openAddIdToPatientScreen(any())
    } else {
      val identifier = Identifier(expectedScannedCode.toString(), BpPassport)
      verify(screen).openAddIdToPatientScreen(identifier)
    }
  }

  //TODO Add verification for camera status show

  @Test
  fun `when the keyboard is up, then hide the QR code scanner view`() {
    // when
    with(uiEvents) {
      onNext(ShowKeyboardEvent)
    }

    // then
    verify(screen).hideQrCodeScannerView()
    verifyNoMoreInteractions(screen)
  }

  @Test
  fun `when the keyboard is up, then don't process valid QR code scan events`() {
    // given
    val bpPassportUUid = "69170f2f-c112-4907-b184-a99d1001e269"
    val patientUuid = UUID.fromString("b123dc5c-a02d-49cb-939e-f4602436f214")
    whenever(patientRepository.findPatientWithBusinessId(bpPassportUUid))
        .thenReturn(Observable.just(PatientMocker.patient(uuid = patientUuid).toOptional()))

    // when
    with(uiEvents) {
      onNext(ShowKeyboardEvent)
      onNext(ValidPassportCode(UUID.fromString(bpPassportUUid)))
    }

    // then
    verify(screen).hideQrCodeScannerView()
    verifyNoMoreInteractions(screen)
  }

  @Test
  fun `when the keyboard is up, then don't process invalid QR code scan events`() {
    // when
    with(uiEvents) {
      onNext(ShowKeyboardEvent)
      onNext(InvalidPassportCode)
    }

    // then
    verify(screen).hideQrCodeScannerView()
    verifyNoMoreInteractions(screen)
  }

  @Test
  fun `when the keyboard is down, then process valid QR code scan events`() {
    // given
    val bpPassportUUid = "69170f2f-c112-4907-b184-a99d1001e269"
    val patientUuid = UUID.fromString("b123dc5c-a02d-49cb-939e-f4602436f214")
    whenever(patientRepository.findPatientWithBusinessId(bpPassportUUid))
        .thenReturn(Observable.just(PatientMocker.patient(uuid = patientUuid).toOptional()))

    // when
    with(uiEvents) {
      onNext(ShowKeyboardEvent)
      onNext(HideKeyboardEvent)
      onNext(ValidPassportCode(UUID.fromString(bpPassportUUid)))
    }

    // then
    verify(screen).openPatientSummary(patientUuid)
    verify(screen).hideQrCodeScannerView()
    verifyNoMoreInteractions(screen)
  }

  @Test
  fun `when the keyboard is down, then don't process invalid QR code events`() {
    // when
    with(uiEvents) {
      onNext(ShowKeyboardEvent)
      onNext(HideKeyboardEvent)
      onNext(InvalidPassportCode)
    }

    // then
    verify(screen).hideQrCodeScannerView()
    verifyNoMoreInteractions(screen)
  }

  @Test
  fun `ignore valid QR code events when the keyboard is up but begin processing incoming valid QR events when the keyboard is down`() {
    // given
    val patientUuid = UUID.fromString("b123dc5c-a02d-49cb-939e-f4602436f214")
    val bpPassportUUid = "69170f2f-c112-4907-b184-a99d1001e269"
    whenever(patientRepository.findPatientWithBusinessId(bpPassportUUid))
        .thenReturn(Observable.just(PatientMocker.patient(uuid = patientUuid).toOptional()))

    // when
    with(uiEvents) {
      onNext(ShowKeyboardEvent)
      onNext(ValidPassportCode(UUID.fromString("39d3296f-7e8d-40c6-8ec8-9a2b083b215a")))
      onNext(HideKeyboardEvent)
      onNext(ValidPassportCode(UUID.fromString(bpPassportUUid)))
    }

    // then
    verify(screen).openPatientSummary(patientUuid)
    verify(screen).hideQrCodeScannerView()
    verifyNoMoreInteractions(screen)
  }

  @Test
  fun `when invalid short code is entered then show validation error`() {
    //given
    val shortCodeText = "3456"
    val shortCodeInput = ShortCodeInput(shortCodeText)

    //when
    uiEvents.onNext(ShortCodeSearched(shortCodeInput))

    //then
    verify(screen).showShortCodeValidationError()
    verifyNoMoreInteractions(screen)
  }

  @Test
  fun `when short code text changes, then hide validation error`() {
    //given
    val invalidShortCode = "3456"
    val invalidShortCodeInput = ShortCodeInput(invalidShortCode)

    uiEvents.onNext(ShortCodeSearched(invalidShortCodeInput))
    verify(screen).showShortCodeValidationError()

    //when
    uiEvents.onNext(ShortCodeChanged)

    //then
    verify(screen).hideShortCodeValidationError()
    verifyNoMoreInteractions(screen)
  }

  @Test
  fun `when user searches with a valid short code, then take the user to the patient search screen`() {
    // given
    val validShortCode = "1234567"
    val validShortCodeInput = ShortCodeInput(validShortCode)

    // when
    uiEvents.onNext(ShortCodeSearched(validShortCodeInput))

    // then
    verify(screen).openPatientShortCodeSearch(validShortCode)
    verifyNoMoreInteractions(screen)
  }

  @Suppress("Unused")
  private fun `params for scanning simple passport qr code`(): List<List<Any>> {
    fun testCase(patient: Optional<Patient>): List<Any> {
      return listOf(UUID.randomUUID(), patient)
    }

    return listOf(
        testCase(None),
        testCase(PatientMocker.patient().toOptional())
    )
  }

  @Suppress("Unused")
  private fun `params for scanning valid uuids`(): List<List<Any?>> {
    fun testCase(scannedTexts: List<String>, expectedUuid: UUID?): List<Any?> {
      return listOf(scannedTexts, expectedUuid)
    }

    return listOf(
        testCase(emptyList(), null),
        testCase(listOf("a"), null),
        testCase(listOf("a", "b2", "c5123"), null),
        testCase(
            listOf("ecf08c6a-2f7e-4163-a6c7-c72a5703422a"),
            UUID.fromString("ecf08c6a-2f7e-4163-a6c7-c72a5703422a")),
        testCase(
            listOf("a2", "ecf08c6a-2f7e-4163-a6c7-c72a5703422a"),
            UUID.fromString("ecf08c6a-2f7e-4163-a6c7-c72a5703422a")),
        testCase(
            listOf("a2", "ecf08c6a-2f7e-4163-a6c7-c72a5703422a", "b5"),
            UUID.fromString("ecf08c6a-2f7e-4163-a6c7-c72a5703422a")),
        testCase(
            listOf("a2", "d7b0cf0b-8467-4969-8f17-f98f48badb5a", "ecf08c6a-2f7e-4163-a6c7-c72a5703422a"),
            UUID.fromString("d7b0cf0b-8467-4969-8f17-f98f48badb5a"))
    )
  }
}
