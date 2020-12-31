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
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import java.util.UUID

class InstantSearchEffectHandlerTest {

  private val facility = TestData.facility()
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
        TestData.patientSearchResult(fullName = "Patient 1"),
        TestData.patientSearchResult(fullName = "Patient 2")
    )
    val searchCriteria = PatientSearchCriteria.Name("Pat")

    whenever(patientRepository.search2(searchCriteria, facility.uuid)) doReturn patients

    // when
    testCase.dispatch(SearchWithCriteria(searchCriteria, facility))

    // then
    testCase.assertOutgoingEvents(SearchResultsLoaded(patients))

    verifyZeroInteractions(uiActions)
  }

  @Test
  fun `when show patients search results effect is received, then show patients search results`() {
    // given
    val facility = TestData.facility()
    val patients = listOf(
        TestData.patientSearchResult(),
        TestData.patientSearchResult()
    )

    // when
    testCase.dispatch(ShowPatientSearchResults(patients, facility))

    // then
    testCase.assertNoOutgoingEvents()

    verify(uiActions).showPatientsSearchResults(patients, facility)
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
    val identifier = TestData.identifier(type = Identifier.IdentifierType.BpPassport, value = "123456")

    // when
    testCase.dispatch(OpenLinkIdWithPatientScreen(patientId, identifier))

    // then
    testCase.assertNoOutgoingEvents()

    verify(uiActions).openLinkIdWithPatientScreen(patientId, identifier)
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when open bp passport sheet effect is received, then open bp passport sheet`() {
    // given
    val identifier = TestData.identifier()

    // when
    testCase.dispatch(OpenBpPassportSheet(identifier))

    // then
    testCase.assertNoOutgoingEvents()

    verify(uiActions).openBpPassportSheet(identifier)
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when show no patients in facility effect is received, then show no patients in facility`() {
    // given
    val facility = TestData.facility()

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
}
