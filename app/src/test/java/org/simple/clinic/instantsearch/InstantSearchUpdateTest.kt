package org.simple.clinic.instantsearch

import androidx.paging.PagingData
import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.NextMatchers.hasNothing
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.instantsearch.InstantSearchProgressState.IN_PROGRESS
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.OngoingNewPatientEntry
import org.simple.clinic.patient.PatientSearchCriteria
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.BpPassport
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.IndiaNationalHealthId
import org.simple.clinic.scanid.scannedqrcode.AddToExistingPatient
import org.simple.clinic.scanid.scannedqrcode.RegisterNewPatient
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

class InstantSearchUpdateTest {

  private val dateOfBirthFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH)
  private val updateSpec = UpdateSpec(InstantSearchUpdate(dateOfBirthFormatter))
  private val identifier = TestData.identifier(
      value = "3e5500fe-e10e-4009-a0bb-3db9009fdef6",
      type = BpPassport
  )
  private val defaultModel = InstantSearchModel.create(identifier, null, null)

  @Test
  fun `when current facility is loaded and search query is not present, then update the model and load all patients`() {
    val facility = TestData.facility(
        uuid = UUID.fromString("a613b2fc-c91c-40a3-9e8b-6da7010ce51b"),
        name = "PHC Obvious"
    )

    updateSpec
        .given(defaultModel)
        .whenEvent(CurrentFacilityLoaded(facility))
        .then(assertThatNext(
            hasModel(defaultModel.facilityLoaded(facility)),
            hasEffects(LoadAllPatients(facility))
        ))
  }

  @Test
  fun `when current facility is loaded and search query is present, then update the model and prefill the search query`() {
    val model = InstantSearchModel.create(
        additionalIdentifier = identifier,
        patientPrefillInfo = null,
        searchQuery = "Pat"
    )
    val facility = TestData.facility(
        uuid = UUID.fromString("a613b2fc-c91c-40a3-9e8b-6da7010ce51b"),
        name = "PHC Obvious"
    )

    updateSpec
        .given(model)
        .whenEvent(CurrentFacilityLoaded(facility))
        .then(assertThatNext(
            hasModel(model.facilityLoaded(facility)),
            hasEffects(PrefillSearchQuery("Pat"))
        ))
  }

  @Test
  fun `when all patients are loaded, then show patient search results if the search query is empty`() {
    val patients = PagingData.from(listOf(
        TestData.patientSearchResult(
            uuid = UUID.fromString("4b991b4d-6c19-4ec5-9524-7d478754775e")
        )
    ))
    val facility = TestData.facility(
        uuid = UUID.fromString("69d8f870-2499-47e3-8775-e39cf7cdab52"),
        name = "PHC Obvious"
    )
    val facilityLoadedModel = defaultModel
        .facilityLoaded(facility)

    updateSpec
        .given(facilityLoadedModel)
        .whenEvent(AllPatientsInFacilityLoaded(patients))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowAllPatients(patients, facility))
        ))
  }

  @Test
  fun `when search results are loaded, then show the patient search results if the query is not empty`() {
    val patients = PagingData.from(listOf(
        TestData.patientSearchResult(
            uuid = UUID.fromString("0f27dabe-5a9e-41ce-bf3c-e0c6fd6a4a6a")
        )
    ))
    val facility = TestData.facility(
        uuid = UUID.fromString("34eb57a9-d80a-4f43-9f89-1e2dade3de3f"),
        name = "PHC Obvious"
    )
    val searchQuery = "Pat"
    val searchQueryModel = defaultModel
        .facilityLoaded(facility)
        .searchQueryChanged("Pat")

    updateSpec
        .given(searchQueryModel)
        .whenEvent(SearchResultsLoaded(patients))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowPatientSearchResults(patients, facility, searchQuery))
        ))
  }

  @Test
  fun `when search query is valid, then load search results`() {
    val facility = TestData.facility(
        uuid = UUID.fromString("f7951ae6-e6c0-4b79-bf3e-2ddd637fa7b4"),
        name = "PHC Obvious"
    )
    val searchQueryModel = defaultModel
        .facilityLoaded(facility)
        .searchQueryChanged("Pat")

    updateSpec
        .given(searchQueryModel)
        .whenEvent(SearchQueryValidated(InstantSearchValidator.Result.Valid("Pat")))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(
                SearchWithCriteria(PatientSearchCriteria.Name("Pat", identifier), facility)
            )
        ))
  }

  @Test
  fun `when search query is empty, then load all patients`() {
    val facility = TestData.facility(
        uuid = UUID.fromString("b3c2ebfb-dfe9-4d8d-8a04-cb29c8f1b9e6"),
        name = "PHC Obvious"
    )
    val facilityLoadedModel = defaultModel
        .facilityLoaded(facility)

    updateSpec
        .given(facilityLoadedModel)
        .whenEvent(SearchQueryValidated(InstantSearchValidator.Result.Empty))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(
                LoadAllPatients(facility)
            )
        ))
  }

  @Test
  fun `when search result is clicked and has no NHID as additional identifier, then open patient summary`() {
    val patientUuid = UUID.fromString("f607be71-630d-4adb-8d3a-76fdf347fe8a")
    val facility = TestData.facility(
        uuid = UUID.fromString("885c6339-9a96-4c8d-bfea-7eea74de6862"),
        name = "PHC Obvious"
    )
    val model = InstantSearchModel
        .create(additionalIdentifier = null, patientPrefillInfo = null, searchQuery = null)
        .facilityLoaded(facility)
        .searchQueryChanged("Pat")

    updateSpec
        .given(model)
        .whenEvent(SearchResultClicked(patientUuid))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(OpenPatientSummary(patientUuid))
        ))
  }

  @Test
  fun `when search result is clicked and has NHID as additional identifier, then check if patient has an existing NHID assigned to them`() {
    val patientUuid = UUID.fromString("f607be71-630d-4adb-8d3a-76fdf347fe8a")
    val facility = TestData.facility(
        uuid = UUID.fromString("885c6339-9a96-4c8d-bfea-7eea74de6862"),
        name = "PHC Obvious"
    )

    val indiaNationalHealthID = "28-3123-2283-6682"
    val patientPrefillInfo = TestData.indiaNHIDInfoPayload(
        healthIdNumber = indiaNationalHealthID
    ).toPatientPrefillInfo()

    val identifier = Identifier(indiaNationalHealthID, IndiaNationalHealthId)

    val model = InstantSearchModel
        .create(additionalIdentifier = identifier,
            patientPrefillInfo = patientPrefillInfo,
            searchQuery = null)
        .facilityLoaded(facility)
        .searchQueryChanged("Pat")

    updateSpec
        .given(model)
        .whenEvent(SearchResultClicked(patientUuid))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(CheckIfPatientAlreadyHasAnExistingNHID(patientUuid))
        ))
  }

  @Test
  fun `when the search result is clicked from the scanning qr code flow, open the link id with patient screen`() {
    val patientUuid = UUID.fromString("f607be71-630d-4adb-8d3a-76fdf347fe8a")
    val identifier = TestData.identifier("123456", BpPassport)
    val facility = TestData.facility(
        uuid = UUID.fromString("658d2987-411e-47a4-97c6-84f8b0f072c0"),
        name = "PHC Obvious"
    )
    val model = InstantSearchModel
        .create(additionalIdentifier = identifier, patientPrefillInfo = null, searchQuery = null)
        .facilityLoaded(facility)
        .searchQueryChanged("Pat")

    updateSpec
        .given(model)
        .whenEvent(SearchResultClicked(patientUuid))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(OpenLinkIdWithPatientScreen(patientUuid, identifier))
        ))
  }

  @Test
  fun `when search query is changed, then search for patients`() {
    val facility = TestData.facility(
        uuid = UUID.fromString("76b89c39-1bc4-4560-9a44-0381c59b58d0"),
        name = "PHC Obvious"
    )
    val facilityLoadedModel = defaultModel
        .facilityLoaded(facility)

    updateSpec
        .given(facilityLoadedModel)
        .whenEvent(SearchQueryChanged("Pat"))
        .then(assertThatNext(
            hasModel(facilityLoadedModel.searchQueryChanged("Pat")),
            hasEffects(ValidateSearchQuery("Pat"))
        ))
  }

  @Test
  fun `when search query is changed and is same as search query in model, then do nothing`() {
    val facility = TestData.facility(
        uuid = UUID.fromString("76b89c39-1bc4-4560-9a44-0381c59b58d0"),
        name = "PHC Obvious"
    )
    val facilityLoadedModel = defaultModel
        .facilityLoaded(facility)
        .searchQueryChanged("Pat")

    updateSpec
        .given(facilityLoadedModel)
        .whenEvent(SearchQueryChanged("Pat"))
        .then(assertThatNext(
            hasNothing()
        ))
  }

  @Test
  fun `when ongoing patient entry is saved, then open patient entry screen`() {
    val facility = TestData.facility()
    val searchQueryModel = defaultModel
        .facilityLoaded(facility)
        .searchQueryChanged("Pat")

    updateSpec
        .given(searchQueryModel)
        .whenEvent(SavedNewOngoingPatientEntry)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(OpenPatientEntryScreen(facility))
        ))
  }

  @Test
  fun `when register new patient is clicked with bp passport, then save ongoing patient entry with identifier and without alternateId id`() {
    val facility = TestData.facility()
    val searchQueryModel = defaultModel
        .facilityLoaded(facility)
        .searchQueryChanged("Pat")

    val ongoingPatientEntry = OngoingNewPatientEntry.fromFullName("Pat")
        .withIdentifier(identifier)
        .copy(alternateId = null)

    updateSpec
        .given(searchQueryModel)
        .whenEvent(RegisterNewPatientClicked)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(SaveNewOngoingPatientEntry(ongoingPatientEntry))
        ))
  }

  @Test
  fun `when register new patient is clicked and patient prefill info is not empty, then save it in ongoing patient entry with alternate id`() {
    val facility = TestData.facility(
        uuid = UUID.fromString("885c6339-9a96-4c8d-bfea-7eea74de6862"),
    )
    val searchQueryModel = defaultModel
        .facilityLoaded(facility)
        .searchQueryChanged("Pat")

    val indiaNationalHealthID = "28-3123-2283-6682"
    val patientPrefillInfo = TestData.indiaNHIDInfoPayload(
        healthIdNumber = indiaNationalHealthID
    ).toPatientPrefillInfo()

    val alternateId = Identifier(indiaNationalHealthID, IndiaNationalHealthId)

    val ongoingNewPatientEntry = OngoingNewPatientEntry(
        personalDetails = OngoingNewPatientEntry.PersonalDetails(
            fullName = patientPrefillInfo.fullName,
            dateOfBirth = patientPrefillInfo.dateOfBirth.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
            gender = Gender.Male,
            age = null),
        address = OngoingNewPatientEntry.Address.BLANK.withColonyOrVillage(patientPrefillInfo.address),
        alternateId = alternateId,
        identifier = null)

    updateSpec
        .given(searchQueryModel.patientPrefillInfoUpdated(patientPrefillInfo).additionalIdentifierUpdated(alternateId))
        .whenEvent(RegisterNewPatientClicked)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(SaveNewOngoingPatientEntry(ongoingNewPatientEntry))
        ))
  }

  @Test
  fun `when register new patient is selected in blank scanned qr code sheet, then register new patient`() {
    val ongoingPatientEntry = OngoingNewPatientEntry.fromFullName("")
        .withIdentifier(identifier)

    val facility = TestData.facility(
        uuid = UUID.fromString("2bd05cc3-5c16-464d-87e1-25b6b1a8a99a")
    )
    val facilityLoadedModel = defaultModel
        .facilityLoaded(facility)

    updateSpec
        .given(facilityLoadedModel)
        .whenEvent(BlankScannedQrCodeResultReceived(RegisterNewPatient))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(SaveNewOngoingPatientEntry(ongoingPatientEntry))
        ))
  }

  @Test
  fun `when add to existing patient is selected in blank scanned qr code sheet, then show keyboard`() {
    val facility = TestData.facility(
        uuid = UUID.fromString("2bd05cc3-5c16-464d-87e1-25b6b1a8a99a")
    )
    val facilityLoadedModel = defaultModel
        .facilityLoaded(facility)

    updateSpec
        .given(facilityLoadedModel)
        .whenEvent(BlankScannedQrCodeResultReceived(AddToExistingPatient))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowKeyboard)
        ))
  }

  @Test
  fun `when open qr code scanner is clicked, then open qr code scanner`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(OpenQrCodeScannerClicked())
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(OpenQrCodeScanner)
        ))
  }

  @Test
  fun `when patient already has an existing national health id, then show national health id error dialog`() {
    val facility = TestData.facility(
        uuid = UUID.fromString("885c6339-9a96-4c8d-bfea-7eea74de6862"),
        name = "PHC Obvious"
    )

    val indiaNationalHealthID = "28-3123-2283-6682"
    val patientPrefillInfo = TestData.indiaNHIDInfoPayload(
        healthIdNumber = indiaNationalHealthID
    ).toPatientPrefillInfo()

    val identifier = Identifier(indiaNationalHealthID, IndiaNationalHealthId)

    val model = InstantSearchModel
        .create(additionalIdentifier = identifier,
            patientPrefillInfo = patientPrefillInfo,
            searchQuery = null)
        .facilityLoaded(facility)
        .searchQueryChanged("Pat")

    updateSpec
        .given(model)
        .whenEvent(PatientAlreadyHasAnExistingNHID)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowNHIDErrorDialog)
        ))
  }

  @Test
  fun `when patient does not have an existing national health id, then open link id with patient screen`() {
    val patientUuid = UUID.fromString("f607be71-630d-4adb-8d3a-76fdf347fe8a")
    val facility = TestData.facility(
        uuid = UUID.fromString("885c6339-9a96-4c8d-bfea-7eea74de6862"),
        name = "PHC Obvious"
    )

    val indiaNationalHealthID = "28-3123-2283-6682"
    val patientPrefillInfo = TestData.indiaNHIDInfoPayload(
        healthIdNumber = indiaNationalHealthID
    ).toPatientPrefillInfo()

    val identifier = Identifier(indiaNationalHealthID, IndiaNationalHealthId)

    val model = InstantSearchModel
        .create(
            additionalIdentifier = identifier,
            patientPrefillInfo = patientPrefillInfo,
            searchQuery = null)
        .facilityLoaded(facility)
        .searchQueryChanged("Pat")

    updateSpec
        .given(model)
        .whenEvent(PatientDoesNotHaveAnExistingNHID(patientUuid))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(OpenLinkIdWithPatientScreen(patientUuid, identifier))
        ))
  }

  @Test
  fun `when search results are being loaded, then update the ui to show progress`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(SearchResultsLoadStateChanged(IN_PROGRESS))
        .then(assertThatNext(
            hasModel(defaultModel.loadStateChanged(IN_PROGRESS)),
            hasNoEffects()
        ))
  }

  @Test
  fun `when instant search screen is shown, then prefill search query if facility is loaded and show keyboard if there is no additional identifier`() {
    val facility = TestData.facility(
        uuid = UUID.fromString("1c3c133a-47f4-4616-9cc9-5c00b0115bd1"),
        name = "PHC Obvious"
    )
    val modelWithoutIdentifier = InstantSearchModel.create(
        additionalIdentifier = null,
        patientPrefillInfo = null,
        searchQuery = "Ram"
    ).facilityLoaded(facility)

    updateSpec
        .given(modelWithoutIdentifier)
        .whenEvent(InstantSearchScreenShown)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowKeyboard, PrefillSearchQuery("Ram"))
        ))
  }
}
