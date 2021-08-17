package org.simple.clinic.scanid

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import com.spotify.mobius.test.RecordingConsumer
import org.junit.After
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.appconfig.Country
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.BpPassport
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.IndiaNationalHealthId
import org.simple.clinic.patient.onlinelookup.api.LookupPatientOnline
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import java.util.UUID

class ScanSimpleIdEffectHandlerTest {

  private val patientRepository = mock<PatientRepository>()
  private val uiActions = mock<ScanSimpleIdUiActions>()
  private val qrCodeJsonParser = mock<QRCodeJsonParser>()
  private val lookupPatientOnline = mock<LookupPatientOnline>()
  private val viewEffectHandler = ScanSimpleIdViewEffectHandler(uiActions)
  private val viewEffectConsumer = viewEffectHandler::handle
  private val testCase = EffectHandlerTestCase(ScanSimpleIdEffectHandler(
      schedulersProvider = TestSchedulersProvider.trampoline(),
      patientRepository = patientRepository,
      qrCodeJsonParser = qrCodeJsonParser,
      country = TestData.country(isoCountryCode = Country.INDIA),
      lookupPatientOnline = lookupPatientOnline,
      viewEffectsConsumer = viewEffectConsumer
  ).build())

  @After
  fun tearDown() {
    testCase.dispose()
  }

  @Test
  fun `when search for patient by identifier effect is received, then search for patient`() {
    // given
    val patient = TestData.patient(
        uuid = UUID.fromString("4db4e9af-56a4-4995-958b-aeb33d80cfa5")
    )

    val identifier = TestData.identifier(
        value = "123 456",
        type = BpPassport
    )

    whenever(patientRepository.findPatientsWithBusinessId(identifier.value)) doReturn listOf(patient)

    // when
    testCase.dispatch(SearchPatientByIdentifier(identifier))

    // then
    testCase.assertOutgoingEvents(PatientSearchByIdentifierCompleted(
        patients = listOf(patient),
        identifier = identifier
    ))
    verifyZeroInteractions(uiActions)
  }

  @Test
  fun `when parse json into patient prefill info object effect is received, then parse the json`() {
    // given
    val expectedJson = """
    {
    "hidn":"12-3412-3456-7856",
    "hid":"Mohit",
    "name":"Mohit Ahuja",
    "gender":"M",
    "statelgd":"Maharashtra",
    "distlgd":"Thane",
    "dob":"12/12/1997",
    "address":"Obvious HQ"
     }
     """
    val indiaNHIDInfoPayload = TestData.indiaNHIDInfoPayload(
        healthIdNumber = "12-3412-3456-7856"
    )

    whenever(qrCodeJsonParser.parseQRCodeJson(expectedJson)) doReturn indiaNHIDInfoPayload
    val patientPrefillInfo = indiaNHIDInfoPayload.toPatientPrefillInfo()
    val healthIdNumber = "12341234567856"

    // when
    testCase.dispatch(ParseScannedJson(expectedJson))

    // then
    testCase.assertOutgoingEvents(ScannedQRCodeJsonParsed(patientPrefillInfo, healthIdNumber))
    verifyZeroInteractions(uiActions)
  }

  @Test
  fun `show invalid qr code, when parsing json outside specified country`() {
    // given
    val testCase = EffectHandlerTestCase(ScanSimpleIdEffectHandler(
        schedulersProvider = TestSchedulersProvider.trampoline(),
        patientRepository = patientRepository,
        qrCodeJsonParser = qrCodeJsonParser,
        country = TestData.country(isoCountryCode = Country.BANGLADESH),
        lookupPatientOnline = lookupPatientOnline,
        viewEffectsConsumer = viewEffectConsumer
    ).build())

    val expectedJson = """
    {
    "hidn":"1234123456785678",
    "hid":"Mohit",
    "name":"Mohit Ahuja",
    "gender":"M",
    "statelgd":"Maharashtra",
    "distlgd":"Thane",
    "dob":"12/12/1997",
    "address":"Obvious HQ"
     }
     """

    // when
    testCase.dispatch(ParseScannedJson(expectedJson))

    // then
    testCase.assertOutgoingEvents(InvalidQrCode)
    verifyZeroInteractions(uiActions)

    testCase.dispose()
  }

  @Test
  fun `when parsing invalid json, then show invalid qr code`() {
    // given
    val expectedJson = """
    {
    "hodn":"1234123456785678",
    "hid":"Mohit",
    "name":"Mohit Ahuja",
    "gender":"M",
    "statelgd":"Maharashtra",
    "distlgd":"Thane",
    "dob":"12/12/1997",
    "address":"Obvious HQ"
     }
     """

    // when
    testCase.dispatch(ParseScannedJson(expectedJson))

    // then
    testCase.assertOutgoingEvents(ScannedQRCodeJsonParsed(patientPrefillInfo = null, healthIdNumber = null))
    verifyZeroInteractions(uiActions)
  }

  @Test
  fun `when open patient summary effect is received, then open patient summary`() {
    // given
    val patientId = UUID.fromString("9730e9a0-e62e-4556-b84e-03d593f6fe4c")

    // when
    testCase.dispatch(OpenPatientSummary(patientId))

    // then
    testCase.assertNoOutgoingEvents()

    verify(uiActions).openPatientSummary(patientId)
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when open patient search effect is received without NHID, then open patient search without patient prefill info`() {
    // given
    val identifier = TestData.identifier(
        value = "a765a30e-6bd9-4f12-99da-acba91b6a479",
        type = BpPassport
    )
    val initialSearchQuery: String? = null

    // when
    testCase.dispatch(OpenPatientSearch(identifier, initialSearchQuery, null))

    // then
    testCase.assertNoOutgoingEvents()

    verify(uiActions).openPatientSearch(identifier, null, null)
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when open patient search effect is received with NHID, then open patient search with patient prefill info`() {
    // given
    val indiaNationalHealthID = "12341234123412"
    val patientPrefillInfo = TestData.patientPrefillInfo()

    val identifier = Identifier(indiaNationalHealthID, IndiaNationalHealthId)

    // when
    testCase.dispatch(OpenPatientSearch(identifier, null, patientPrefillInfo))

    // then
    testCase.assertNoOutgoingEvents()

    verify(uiActions).openPatientSearch(identifier, null, patientPrefillInfo)
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when online patient lookup with identifier effect is received, then lookup patients online with the patient not found error`() {
    // given
    val identifier = TestData.identifier(
        value = "e2696e35-45e9-4982-8555-3de7a7aa4dea",
        type = BpPassport
    )
    val results = LookupPatientOnline.Result.NotFound(identifier = identifier.value)
    whenever(lookupPatientOnline.lookupWithIdentifier(identifier.value)) doReturn results

    // when
    testCase.dispatch(OnlinePatientLookupWithIdentifier(identifier))

    // then
    testCase.assertOutgoingEvents(OnlinePatientLookupWithIdentifierCompleted(results, identifier))
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when online patient lookup with identifier effect is received, then lookup patients online with the other error`() {
    // given
    val identifier = TestData.identifier(
        value = "91704c43-c35f-41d2-9887-d5cb6e580dd9",
        type = IndiaNationalHealthId
    )
    val results = LookupPatientOnline.Result.OtherError
    whenever(lookupPatientOnline.lookupWithIdentifier(identifier.value)) doReturn results

    // when
    testCase.dispatch(OnlinePatientLookupWithIdentifier(identifier))

    // then
    testCase.assertOutgoingEvents(OnlinePatientLookupWithIdentifierCompleted(results, identifier))
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when save complete medical records effect is received, then save the patient medical records`() {
    // given
    val identifier = Identifier("4f1cea37-70ff-498e-bd09-ad0ca75628ff", BpPassport)
    val commonIdentifier = TestData.businessId(identifier = identifier)

    val patientUuid1 = TestData.patientProfile(patientUuid = UUID.fromString("0b78c024-f527-4306-9e20-6ae6d7251e9b"), businessId = commonIdentifier)
    val patientUuid2 = TestData.patientProfile(patientUuid = UUID.fromString("47fdb968-9512-4e50-b95f-cc83c6de4b0a"), businessId = commonIdentifier)

    val completeMedicalRecord = TestData.completeMedicalRecord(patient = patientUuid1)
    val completeMedicalRecord2 = TestData.completeMedicalRecord(patient = patientUuid2)

    val medicalRecords = listOf(completeMedicalRecord, completeMedicalRecord2)

    // when
    testCase.dispatch(SaveCompleteMedicalRecords(medicalRecords))

    // then
    verify(patientRepository).saveCompleteMedicalRecord(medicalRecords)
    testCase.assertOutgoingEvents(CompleteMedicalRecordsSaved(medicalRecords))
    verifyNoMoreInteractions(uiActions)
  }
}
