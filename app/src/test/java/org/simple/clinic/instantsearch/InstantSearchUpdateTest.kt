package org.simple.clinic.instantsearch

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.patient.PatientSearchCriteria
import org.simple.clinic.patient.businessid.Identifier
import java.util.UUID

class InstantSearchUpdateTest {

  private val updateSpec = UpdateSpec(InstantSearchUpdate())
  private val identifier = TestData.identifier()
  private val defaultModel = InstantSearchModel.create(identifier)

  @Test
  fun `when current facility is loaded, then update the model and load all patients`() {
    val facility = TestData.facility()

    updateSpec
        .given(defaultModel)
        .whenEvent(CurrentFacilityLoaded(facility))
        .then(assertThatNext(
            hasModel(defaultModel.facilityLoaded(facility)),
            hasEffects(LoadAllPatients(facility))
        ))
  }

  @Test
  fun `when all patients are loaded, then show patient search results if the search query is empty`() {
    val patients = listOf(
        TestData.patientSearchResult()
    )
    val facility = TestData.facility()
    val facilityLoadedModel = defaultModel
        .facilityLoaded(facility)

    updateSpec
        .given(facilityLoadedModel)
        .whenEvent(AllPatientsLoaded(patients))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowPatientSearchResults(patients, facility))
        ))
  }

  @Test
  fun `when all patients list is empty, then show no patients in facility`() {
    val facility = TestData.facility()
    val facilityLoadedModel = defaultModel
        .facilityLoaded(facility)

    updateSpec
        .given(facilityLoadedModel)
        .whenEvent(AllPatientsLoaded(emptyList()))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowNoPatientsInFacility(facility))
        ))
  }

  @Test
  fun `when search results are loaded, then show the patient search results if the query is not empty`() {
    val patients = listOf(
        TestData.patientSearchResult()
    )
    val facility = TestData.facility()
    val searchQueryModel = defaultModel
        .facilityLoaded(facility)
        .searchQueryChanged("Pat")

    updateSpec
        .given(searchQueryModel)
        .whenEvent(SearchResultsLoaded(patients))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowPatientSearchResults(patients, facility))
        ))
  }

  @Test
  fun `when search results are empty, then show no search results`() {
    val facility = TestData.facility()
    val searchQueryModel = defaultModel
        .facilityLoaded(facility)
        .searchQueryChanged("Pat")

    updateSpec
        .given(searchQueryModel)
        .whenEvent(SearchResultsLoaded(emptyList()))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowNoSearchResults)
        ))
  }

  @Test
  fun `when search query is valid, then load search results`() {
    val facility = TestData.facility()
    val searchQueryModel = defaultModel
        .facilityLoaded(facility)
        .searchQueryChanged("Pat")

    updateSpec
        .given(searchQueryModel)
        .whenEvent(SearchQueryValidated(InstantSearchValidator.Result.Valid("Pat")))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(
                HideNoPatientsInFacility,
                HideNoSearchResults,
                SearchWithCriteria(PatientSearchCriteria.Name("Pat", identifier), facility)
            )
        ))
  }

  @Test
  fun `when search query is empty, then load all patients`() {
    val facility = TestData.facility()
    val facilityLoadedModel = defaultModel
        .facilityLoaded(facility)

    updateSpec
        .given(facilityLoadedModel)
        .whenEvent(SearchQueryValidated(InstantSearchValidator.Result.Empty))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(
                HideNoSearchResults,
                LoadAllPatients(facility)
            )
        ))
  }

  @Test
  fun `when search result is clicked, then open patient summary`() {
    val patientUuid = UUID.fromString("f607be71-630d-4adb-8d3a-76fdf347fe8a")
    val facility = TestData.facility()
    val model = InstantSearchModel
        .create(additionalIdentifier = null)
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
  fun `when search result is clicked and has additional identifier, then open patient summary`() {
    val patientUuid = UUID.fromString("f607be71-630d-4adb-8d3a-76fdf347fe8a")
    val identifier = TestData.identifier("123456", Identifier.IdentifierType.BpPassport)
    val facility = TestData.facility()
    val model = InstantSearchModel
        .create(additionalIdentifier = identifier)
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
    val facility = TestData.facility()
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
}
