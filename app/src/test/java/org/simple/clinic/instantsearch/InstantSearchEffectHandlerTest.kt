package org.simple.clinic.instantsearch

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import org.junit.After
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.PatientSearchCriteria
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.BpPassport
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.IndiaNationalHealthId
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import org.simple.clinic.util.toOptional
import java.util.UUID

class InstantSearchEffectHandlerTest {

  private val facility = TestData.facility(
      uuid = UUID.fromString("9cb066b5-ffa4-412e-b345-dfa98850fcce"),
      name = "PHC Obvious"
  )
  private val patientRepository = mock<PatientRepository>()
  private val uiActions = mock<InstantSearchUiActions>()
  private val effectHandler = InstantSearchEffectHandler(
      currentFacility = { facility },
      patientRepository = patientRepository,
      instantSearchValidator = InstantSearchValidator(),
      instantSearchConfig = InstantSearchConfig(
          minLengthOfSearchQuery = 2
      ),
      schedulers = TestSchedulersProvider.trampoline(),
      uiActions = uiActions
  ).build()
  private val testCase = EffectHandlerTestCase(effectHandler)

  @After
  fun tearDown() {
    testCase.dispose()
  }

  @Test
  fun `when load current facility effect is received, then load the current facility`() {
    // when
    testCase.dispatch(LoadCurrentFacility)

    // then
    testCase.assertOutgoingEvents(CurrentFacilityLoaded(facility))
  }

  @Test
  fun `when load all patients effect is received, then load all patients`() {
    // given
    val patients = listOf(
        TestData.patientSearchResult(uuid = UUID.fromString("ba579c2a-e067-4ded-ab4e-86589414c6d0")),
        TestData.patientSearchResult(uuid = UUID.fromString("24be0305-04a3-4111-94e2-e0a254e38a04"))
    )

    whenever(patientRepository.allPatientsInFacility(facility)) doReturn patients

    // when
    testCase.dispatch(LoadAllPatients(facility))

    // then
    testCase.assertOutgoingEvents(AllPatientsLoaded(patients))

    verifyZeroInteractions(uiActions)
  }

  @Test
  fun `when search by criteria effect is received, then search by criteria`() {
    // given
    val patients = listOf(
        TestData.patientSearchResult(
            uuid = UUID.fromString("c9ecb8c1-93a2-4a9e-92ee-2231670ef91e"),
            fullName = "Patient 1"
        ),
        TestData.patientSearchResult(
            uuid = UUID.fromString("4e615da0-eed2-4e8d-b9e9-a84021db9d3d"),
            fullName = "Patient 2"
        )
    )
    val searchCriteria = PatientSearchCriteria.Name("Pat")

    whenever(patientRepository.search(searchCriteria, facility.uuid)) doReturn patients

    // when
    testCase.dispatch(SearchWithCriteria(searchCriteria, facility))

    // then
    testCase.assertOutgoingEvents(SearchResultsLoaded(patients))

    verifyZeroInteractions(uiActions)
  }

  @Test
  fun `when show all patients effect is received, then show all patients`() {
    // given
    val facility = TestData.facility(
        uuid = UUID.fromString("f1e9ad5c-7de0-4566-b1fc-392bdfdc8490"),
        name = "PHC Obvious"
    )
    val patients = listOf(
        TestData.patientSearchResult(
            uuid = UUID.fromString("14edda47-c177-4b5b-9d72-832e262255a3")
        ),
        TestData.patientSearchResult(
            uuid = UUID.fromString("a96ebfe1-a59c-4518-86ef-2ad2174cca03")
        )
    )

    // when
    testCase.dispatch(ShowAllPatients(patients, facility))

    // then
    testCase.assertNoOutgoingEvents()

    verify(uiActions).showAllPatients(patients, facility)
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when show patients search results effect is received, then show patients search results`() {
    // given
    val searchQuery = "P"
    val facility = TestData.facility(
        uuid = UUID.fromString("f1e9ad5c-7de0-4566-b1fc-392bdfdc8490"),
        name = "PHC Obvious"
    )
    val patients = listOf(
        TestData.patientSearchResult(
            uuid = UUID.fromString("14edda47-c177-4b5b-9d72-832e262255a3")
        ),
        TestData.patientSearchResult(
            uuid = UUID.fromString("a96ebfe1-a59c-4518-86ef-2ad2174cca03")
        )
    )

    // when
    testCase.dispatch(ShowPatientSearchResults(patients, facility, searchQuery))

    // then
    testCase.assertNoOutgoingEvents()

    verify(uiActions).showPatientsSearchResults(patients, facility, searchQuery)
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when validate search query effect is received, then validate search query`() {
    // given
    val validationResult = InstantSearchValidator.Result.Valid("Pat")

    // when
    testCase.dispatch(ValidateSearchQuery("Pat"))

    // then
    testCase.assertOutgoingEvents(SearchQueryValidated(validationResult))

    verifyZeroInteractions(uiActions)
  }

  @Test
  fun `when open patient summary effect is received, then open patient summary`() {
    // given
    val patientId = UUID.fromString("da014c89-a32b-4236-a811-357590b57b99")

    // when
    testCase.dispatch(OpenPatientSummary(patientId))

    // then
    testCase.assertNoOutgoingEvents()

    verify(uiActions).openPatientSummary(patientId)
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when open link id with patient screen effect is received, then open link id with patient screen`() {
    // given
    val patientId = UUID.fromString("f7242bcf-585d-4f4e-81ff-407ccc2d7554")
    val identifier = TestData.identifier(
        type = BpPassport,
        value = "96a58c7c-e516-42ed-a635-044cc89d6e64"
    )

    // when
    testCase.dispatch(OpenLinkIdWithPatientScreen(patientId, identifier))

    // then
    testCase.assertNoOutgoingEvents()

    verify(uiActions).openLinkIdWithPatientScreen(patientId, identifier)
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when open scanned qr code sheet effect is received, then open scanned qr code sheet`() {
    // given
    val identifier = TestData.identifier(
        value = "08d5528b-8587-4ada-9b6a-4ff07b9b3357",
        type = BpPassport
    )

    // when
    testCase.dispatch(OpenScannedQrCodeSheet(identifier))

    // then
    testCase.assertNoOutgoingEvents()

    verify(uiActions).openScannedQrCodeSheet(identifier)
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when show no patients in facility effect is received, then show no patients in facility`() {
    // given
    val facility = TestData.facility(
        uuid = UUID.fromString("6889f6fb-aa9f-4e5f-8d48-4d22420bd811"),
        name = "PHC Obvious"
    )

    // when
    testCase.dispatch(ShowNoPatientsInFacility(facility))

    // then
    testCase.assertNoOutgoingEvents()

    verify(uiActions).showNoPatientsInFacility(facility)
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when hide no patients in facility effect is received, then hide no patients in facility`() {
    // when
    testCase.dispatch(HideNoPatientsInFacility)

    // then
    testCase.assertNoOutgoingEvents()

    verify(uiActions).hideNoPatientsInFacility()
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when show no search results effect is received, then show no search results`() {
    // when
    testCase.dispatch(ShowNoSearchResults)

    // then
    testCase.assertNoOutgoingEvents()

    verify(uiActions).showNoSearchResults()
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when hide no search results effect is received, then hide no search results`() {
    // when
    testCase.dispatch(HideNoSearchResults)

    // then
    testCase.assertNoOutgoingEvents()

    verify(uiActions).hideNoSearchResults()
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when save on going patient entry effect is received, then save on going patient entry`() {
    // given
    val ongoingNewPatientEntry = TestData.ongoingPatientEntry()

    // when
    testCase.dispatch(SaveNewOngoingPatientEntry(ongoingNewPatientEntry))

    // then
    testCase.assertOutgoingEvents(SavedNewOngoingPatientEntry)

    verifyZeroInteractions(uiActions)
  }

  @Test
  fun `when open patient entry screen effect is received, then open patient entry screen`() {
    // given
    val facility = TestData.facility()

    // when
    testCase.dispatch(OpenPatientEntryScreen(facility))

    // then
    testCase.assertNoOutgoingEvents()

    verify(uiActions).openPatientEntryScreen(facility)
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when show keyboard effect is received, then show keyboard`() {
    // when
    testCase.dispatch(ShowKeyboard)

    // then
    testCase.assertNoOutgoingEvents()

    verify(uiActions).showKeyboard()
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when open qr code scanner effect is received, then open the qr code scanner`() {
    // when
    testCase.dispatch(OpenQrCodeScanner)

    // then
    testCase.assertNoOutgoingEvents()

    verify(uiActions).openQrCodeScanner()
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when check if patient has an existing NHID effect is received and patient does have an existing NHID, then return patient has an existing NHID event`() {
    val patientId = UUID.fromString("f7242bcf-585d-4f4e-81ff-407ccc2d7554")
    val businessId = UUID.fromString("6889f6fb-aa9f-4e5f-8d48-4d22420bd811")
    val patientProfile = TestData.patientProfile(
        patientUuid = patientId,
        patientName = "Pat",
        businessId = TestData.businessId(
            uuid = businessId,
            identifier = Identifier(
                value = "28-3123-2283-6682",
                type = IndiaNationalHealthId,
            ),
            patientUuid = patientId))

    whenever(patientRepository.patientProfileImmediate(patientId)).thenReturn(patientProfile.toOptional())

    // when
    testCase.dispatch(CheckIfPatientAlreadyHasAnExistingNHID(patientId))

    //then
    testCase.assertOutgoingEvents(PatientAlreadyHasAnExistingNHID)
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when check if patient has an existing NHID effect is received and patient does not have an existing NHID, then return patient does not have an existing NHID event`() {
    val patientId = UUID.fromString("f7242bcf-585d-4f4e-81ff-407ccc2d7554")
    val businessId = UUID.fromString("6889f6fb-aa9f-4e5f-8d48-4d22420bd811")
    val patientProfile = TestData.patientProfile(
        patientUuid = patientId,
        patientName = "Pat",
        businessId = TestData.businessId(
            uuid = businessId,
            identifier = Identifier(
                value = "08d5528b-8587-4ada-9b6a-4ff07b9b3357",
                type = BpPassport,
            ),
            patientUuid = patientId))

    whenever(patientRepository.patientProfileImmediate(patientId)).thenReturn(patientProfile.toOptional())

    // when
    testCase.dispatch(CheckIfPatientAlreadyHasAnExistingNHID(patientId))

    //then
    testCase.assertOutgoingEvents(PatientDoesNotHaveAnExistingNHID(patientId))
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when show NHID error dialog effect is received, show NHID error dialog`() {
    // when
    testCase.dispatch(ShowNHIDErrorDialog)

    //then
    testCase.assertNoOutgoingEvents()
    verify(uiActions).showNHIDErrorDialog()
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when prefill initial search query effect is received, then prefill instant search query`() {
    // given
    val initialSearchQuery = "Ramesh"

    // when
    testCase.dispatch(PrefillSearchQuery(initialSearchQuery))

    // then
    testCase.assertNoOutgoingEvents()
    verify(uiActions).prefillSearchQuery(initialSearchQuery)
    verifyNoMoreInteractions(uiActions)
  }
}
