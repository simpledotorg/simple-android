package org.simple.clinic.scanid

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.BpPassport
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.IndiaNationalHealthId
import org.simple.clinic.patient.onlinelookup.api.LookupPatientOnline
import java.util.UUID

class ScanSimpleIdUpdateTest {

  private val defaultModel = ScanSimpleIdModel.create()
  private val expectedJson = """
    {
    "hidn":"28-3222-2283-6682",
    "hid":"mogithduraisamy@ndhm",
    "name":"Mogith",
    "gender":"M",
    "statelgd":"34",
    "distlgd":"600",
    "dob":"25/6/2012",
    "address":"No.42 eswaran kovil street"
     }
     """

  private val spec = UpdateSpec(ScanSimpleIdUpdate(
      isIndianNHIDSupportEnabled = true,
      isOnlinePatientLookupEnabled = true
  ))

  @Test
  fun `when a valid QR code is scanned, then search for the patient`() {
    val scannedId = "9f154761-ee2f-4ee3-acd1-0038328f75ca"
    val identifier = Identifier(scannedId, BpPassport)

    spec
        .given(defaultModel)
        .whenEvent(ScanSimpleIdScreenQrCodeScanned(scannedId))
        .then(assertThatNext(
            hasModel(defaultModel
                .clearInvalidQrCodeError()
                .searching()),
            hasEffects(SearchPatientByIdentifier(identifier))
        ))
  }

  @Test
  fun `when the qr code is scanned and the returned string is json then parse the json into object`() {
    spec
        .given(defaultModel)
        .whenEvent(ScanSimpleIdScreenQrCodeScanned(expectedJson))
        .then(assertThatNext(
            hasModel(defaultModel
                .clearInvalidQrCodeError()
                .searching()),
            hasEffects(ParseScannedJson(expectedJson))
        ))
  }

  @Test
  fun `when json is parsed with a valid health id number, then search patient with NHID and save patient prefill info`() {
    val indiaNationalHealthID = "12341234123412"
    val indiaNHIDInfoPayload = TestData.indiaNHIDInfoPayload(
        healthIdNumber = indiaNationalHealthID
    )
    val indiaNHIDInfo = indiaNHIDInfoPayload.healthIdNumber
    val patientPrefillInfo = indiaNHIDInfoPayload.toPatientPrefillInfo()

    val identifier = Identifier(indiaNationalHealthID, IndiaNationalHealthId)

    spec
        .given(defaultModel)
        .whenEvent(ScannedQRCodeJsonParsed(patientPrefillInfo, indiaNHIDInfo))
        .then(assertThatNext(
            hasModel(defaultModel.searching().patientPrefillInfoChanged(patientPrefillInfo)),
            hasEffects(SearchPatientByIdentifier(identifier))
        ))
  }

  @Test
  fun `when json is parsed with an invalid health id number, then search patient with NHID and save patient prefill info`() {
    val indiaNationalHealthID = ""
    val indiaNHIDInfoPayload = TestData.indiaNHIDInfoPayload(
        healthIdNumber = indiaNationalHealthID
    )
    val indiaNHIDInfo = indiaNHIDInfoPayload.healthIdNumber
    val patientPrefillInfo = indiaNHIDInfoPayload.toPatientPrefillInfo()

    spec
        .given(defaultModel)
        .whenEvent(ScannedQRCodeJsonParsed(patientPrefillInfo, indiaNHIDInfo))
        .then(assertThatNext(
            hasModel(defaultModel.notSearching().invalidQrCode()),
            hasNoEffects()
        ))
  }

  @Test
  fun `when the entered short code is valid, then open patient search`() {
    val enteredShortCode = "1234567"
    val model = defaultModel.enteredCodeChanged(EnteredCodeInput(enteredShortCode))

    spec
        .given(model)
        .whenEvent(EnteredCodeValidated(EnteredCodeValidationResult.Success))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(OpenPatientSearch(additionalIdentifier = null, initialSearchQuery = enteredShortCode, patientPrefillInfo = null))
        ))
  }

  @Test
  fun `when identifier is scanned and patient is found, then open patient summary`() {
    val patientId = UUID.fromString("60822507-9151-4836-944b-9cbbd1530c0b")
    val patient = TestData.patient(
        uuid = patientId
    )
    val identifier = Identifier("77877995-6f2f-4591-bdb8-bd0f9c62d573", BpPassport)

    spec
        .given(defaultModel)
        .whenEvent(PatientSearchByIdentifierCompleted(listOf(patient), identifier))
        .then(assertThatNext(
            hasModel(defaultModel.notSearching()),
            hasEffects(OpenPatientSummary(patientId))
        ))
  }

  @Test
  fun `when identifier is scanned and patient is not found, then lookup patient online`() {
    val patients = emptyList<Patient>()
    val identifier = Identifier("6e2a631f-dc39-45c3-8608-160d0cc1e36b", BpPassport)

    spec
        .given(defaultModel)
        .whenEvent(PatientSearchByIdentifierCompleted(patients, identifier))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(OnlinePatientLookupWithIdentifier(identifier))
        ))
  }

  @Test
  fun `when NHID is scanned and patient is not found, then send the identifier to patient search screen with patient prefill info`() {
    val patients = emptyList<Patient>()

    val indiaNationalHealthID = "12341234123412"
    val indiaNHIDInfoPayload = TestData.indiaNHIDInfoPayload(
        healthIdNumber = indiaNationalHealthID
    )
    val patientPrefillInfo = indiaNHIDInfoPayload.toPatientPrefillInfo()

    val identifier = Identifier(indiaNationalHealthID, IndiaNationalHealthId)

    val patientPrefillInfoChangedModel = defaultModel.patientPrefillInfoChanged(patientPrefillInfo)

    spec
        .given(patientPrefillInfoChangedModel)
        .whenEvent(PatientSearchByIdentifierCompleted(patients, identifier))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(OnlinePatientLookupWithIdentifier(identifier))
        ))
  }

  @Test
  fun `when identifier is scanned and more than 1 patient is found with bp passport, then open patient search`() {
    val patientId1 = UUID.fromString("60822507-9151-4836-944b-9cbbd1530c0b")
    val patientId2 = UUID.fromString("de90d491-29ab-4bb7-938c-d436815794c6")

    val patient1 = TestData.patient(uuid = patientId1)
    val patient2 = TestData.patient(uuid = patientId2)

    val identifier = Identifier("47d51ebf-0815-4f30-94aa-bc210b305935", BpPassport)

    spec
        .given(defaultModel)
        .whenEvent(PatientSearchByIdentifierCompleted(listOf(patient1, patient2), identifier))
        .then(assertThatNext(
            hasModel(defaultModel.notSearching()),
            hasEffects(OpenPatientSearch(additionalIdentifier = null, initialSearchQuery = "4751081", patientPrefillInfo = null))
        ))
  }

  @Test
  fun `when identifier is scanned and more than 1 patient is found with id, then open patient search`() {
    val patientId1 = UUID.fromString("60822507-9151-4836-944b-9cbbd1530c0b")
    val patientId2 = UUID.fromString("de90d491-29ab-4bb7-938c-d436815794c6")

    val patient1 = TestData.patient(uuid = patientId1)
    val patient2 = TestData.patient(uuid = patientId2)

    val identifier = Identifier("12345612345612", IndiaNationalHealthId)

    spec
        .given(defaultModel)
        .whenEvent(PatientSearchByIdentifierCompleted(listOf(patient1, patient2), identifier))
        .then(assertThatNext(
            hasModel(defaultModel.notSearching()),
            hasEffects(OpenPatientSearch(null, "12345612345612", null))
        ))
  }

  @Test
  fun `when entered code is not a short code, then open patient search`() {
    val enteredCode = "12345678901234"
    val model = defaultModel.enteredCodeChanged(EnteredCodeInput(enteredCode))

    spec
        .given(model)
        .whenEvent(EnteredCodeValidated(EnteredCodeValidationResult.Success))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(OpenPatientSearch(additionalIdentifier = null, initialSearchQuery = enteredCode, null))
        ))
  }

  @Test
  fun `when invalid qr code is scanned, then update model`() {
    spec
        .given(defaultModel)
        .whenEvent(InvalidQrCode)
        .then(assertThatNext(
            hasModel(defaultModel.notSearching().invalidQrCode()),
            hasNoEffects()
        ))
  }

  @Test
  fun `when entered code is changed, then hide invalid qr code error`() {
    val model = defaultModel
        .invalidQrCode()

    spec
        .given(model)
        .whenEvent(EnteredCodeChanged)
        .then(assertThatNext(
            hasModel(model.clearInvalidQrCodeError()),
            hasEffects(HideEnteredCodeValidationError)
        ))
  }

  @Test
  fun `when online patient lookup is completed and patient is not found, show patient search`() {
    val identifier = Identifier("e96c20ec-2518-4774-a92d-ee8d5c02b875", BpPassport)

    spec
        .given(defaultModel)
        .whenEvent(OnlinePatientLookupWithIdentifierCompleted(LookupPatientOnline.Result.NotFound(identifier.value), identifier))
        .then(
            assertThatNext(
                hasModel(defaultModel.notSearching()),
                hasEffects(OpenPatientSearch(identifier, null, null))
            )
        )
  }

  @Test
  fun `when online patient lookup is completed and had other error, show patient search`() {
    val identifier = Identifier("fe1487af-024f-4448-8361-325913e6db81", BpPassport)

    spec
        .given(defaultModel)
        .whenEvent(OnlinePatientLookupWithIdentifierCompleted(LookupPatientOnline.Result.OtherError, identifier))
        .then(
            assertThatNext(
                hasModel(defaultModel.notSearching()),
                hasEffects(OpenPatientSearch(identifier, null, null))
            )
        )
  }

  @Test
  fun `When complete medical records are saved and one patient is found, open patient summary`() {
    val completeMedicalRecord = TestData.completeMedicalRecord()
    val patientUuid = completeMedicalRecord.patient.patientUuid

    spec
        .given(defaultModel)
        .whenEvent(CompleteMedicalRecordsSaved(listOf(completeMedicalRecord)))
        .then(
            assertThatNext(
                hasNoModel(),
                hasEffects(OpenPatientSummary(patientUuid))
            )
        )
  }

  @Test
  fun `when online patient lookup is completed and had found medical records, save complete medical records`() {
    val identifier = Identifier("fe1487af-024f-4448-8361-325913e6db81", BpPassport)
    val commonIdentifier = TestData.businessId(identifier = identifier)

    val patientUuid1 = TestData.patientProfile(patientUuid = UUID.fromString("da01a72e-cc74-4e74-86ff-59d730dfc2eb"), businessId = commonIdentifier)
    val patientUuid2 = TestData.patientProfile(patientUuid = UUID.fromString("a09acec5-7cab-4b1a-a99a-ef673b29e024"), businessId = commonIdentifier)

    val completeMedicalRecord = TestData.completeMedicalRecord(patient = patientUuid1)
    val completeMedicalRecord2 = TestData.completeMedicalRecord(patient = patientUuid2)

    val medicalRecords = listOf(completeMedicalRecord, completeMedicalRecord2)

    spec
        .given(defaultModel)
        .whenEvent(OnlinePatientLookupWithIdentifierCompleted(LookupPatientOnline.Result.Found(medicalRecords = medicalRecords), identifier))
        .then(
            assertThatNext(
                hasModel(defaultModel.notSearching()),
                hasEffects(SaveCompleteMedicalRecords(medicalRecords))
            )
        )
  }

  @Test
  fun `When complete medical records are saved and more than 1 patient is found with id then open patient search`() {
    val identifier = Identifier("8c76b646-c03f-4d9c-8140-ec08e1945051", BpPassport)
    val commonIdentifier = TestData.businessId(identifier = identifier)

    val patientUuid1 = TestData.patientProfile(patientUuid = UUID.fromString("69beb7f2-b97f-452b-8faa-d98dfa33ce82"), businessId = commonIdentifier)
    val patientUuid2 = TestData.patientProfile(patientUuid = UUID.fromString("17817ea4-f6d0-453d-b739-648000f1db97"), businessId = commonIdentifier)

    val completeMedicalRecord = TestData.completeMedicalRecord(patient = patientUuid1)
    val completeMedicalRecord2 = TestData.completeMedicalRecord(patient = patientUuid2)

    spec
        .given(defaultModel)
        .whenEvent(CompleteMedicalRecordsSaved(listOf(completeMedicalRecord, completeMedicalRecord2)))
        .then(
            assertThatNext(
                hasNoModel(),
                hasEffects(OpenPatientSearch(additionalIdentifier = null, "8766460", null))
            )
        )
  }

  @Test
  fun `when identifier is scanned and patient is not found and lookup patient online is disabled, then open patient search`() {
    val spec = UpdateSpec(ScanSimpleIdUpdate(
        isIndianNHIDSupportEnabled = true,
        isOnlinePatientLookupEnabled = false
    ))

    val patients = emptyList<Patient>()
    val identifier = Identifier("0486cdb8-b617-41bf-9b9f-4a89434c68cf", BpPassport)

    spec
        .given(defaultModel)
        .whenEvent(PatientSearchByIdentifierCompleted(patients, identifier))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(OpenPatientSearch(additionalIdentifier = identifier, initialSearchQuery = null, patientPrefillInfo = null))
        ))
  }
}
