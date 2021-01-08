package org.simple.clinic.instantsearch

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.patient.OngoingNewPatientEntry
import org.simple.clinic.patient.PatientSearchCriteria
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.BpPassport
import java.util.UUID

class InstantSearchUpdateTest {

  private val updateSpec = UpdateSpec(InstantSearchUpdate())
  private val identifier = TestData.identifier(
      value = "3e5500fe-e10e-4009-a0bb-3db9009fdef6",
      type = BpPassport
  )
  private val defaultModel = InstantSearchModel.create(identifier)

  @Test
  fun `when current facility is loaded, then update the model and load all patients`() {
    val facility = TestData.facility(
        uuid = UUID.fromString("a613b2fc-c91c-40a3-9e8b-6da7010ce51b"),
        name = "PHC Obvious"
    )

    updateSpec
        .given(defaultModel)
        .whenEvent(CurrentFacilityLoaded(facility))
        .then(assertThatNext(
            hasModel(defaultModel.facilityLoaded(facility).loadingAllPatients()),
            hasEffects(LoadAllPatients(facility))
        ))
  }

  @Test
  fun `when all patients are loaded, then show patient search results if the search query is empty`() {
    val patients = listOf(
        TestData.patientSearchResult(
            uuid = UUID.fromString("4b991b4d-6c19-4ec5-9524-7d478754775e")
        )
    )
    val facility = TestData.facility(
        uuid = UUID.fromString("69d8f870-2499-47e3-8775-e39cf7cdab52"),
        name = "PHC Obvious"
    )
    val facilityLoadedModel = defaultModel
        .facilityLoaded(facility)

    updateSpec
        .given(facilityLoadedModel)
        .whenEvent(AllPatientsLoaded(patients))
        .then(assertThatNext(
            hasModel(facilityLoadedModel.allPatientsLoaded()),
            hasEffects(ShowPatientSearchResults(patients, facility))
        ))
  }

  @Test
  fun `when all patients list is empty, then show no patients in facility`() {
    val facility = TestData.facility(
        uuid = UUID.fromString("3ccb34f2-dabb-4baa-8576-00fe59827682"),
        name = "PHC Obvious"
    )
    val facilityLoadedModel = defaultModel
        .facilityLoaded(facility)

    updateSpec
        .given(facilityLoadedModel)
        .whenEvent(AllPatientsLoaded(emptyList()))
        .then(assertThatNext(
            hasModel(facilityLoadedModel.allPatientsLoaded()),
            hasEffects(ShowNoPatientsInFacility(facility))
        ))
  }

  @Test
  fun `when search results are loaded, then show the patient search results if the query is not empty`() {
    val patients = listOf(
        TestData.patientSearchResult(
            uuid = UUID.fromString("0f27dabe-5a9e-41ce-bf3c-e0c6fd6a4a6a")
        )
    )
    val facility = TestData.facility(
        uuid = UUID.fromString("34eb57a9-d80a-4f43-9f89-1e2dade3de3f"),
        name = "PHC Obvious"
    )
    val searchQueryModel = defaultModel
        .facilityLoaded(facility)
        .searchQueryChanged("Pat")

    updateSpec
        .given(searchQueryModel)
        .whenEvent(SearchResultsLoaded(patients))
        .then(assertThatNext(
            hasModel(searchQueryModel.searchResultsLoaded()),
            hasEffects(ShowPatientSearchResults(patients, facility))
        ))
  }

  @Test
  fun `when search results are empty, then show no search results`() {
    val facility = TestData.facility(
        uuid = UUID.fromString("9cede3eb-e47a-47df-b14e-10eefc6b272f"),
        name = "PHC Obvious"
    )
    val searchQueryModel = defaultModel
        .facilityLoaded(facility)
        .searchQueryChanged("Pat")

    updateSpec
        .given(searchQueryModel)
        .whenEvent(SearchResultsLoaded(emptyList()))
        .then(assertThatNext(
            hasModel(searchQueryModel.searchResultsLoaded()),
            hasEffects(ShowNoSearchResults)
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
            hasModel(searchQueryModel.loadingSearchResults()),
            hasEffects(
                HideNoPatientsInFacility,
                HideNoSearchResults,
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
            hasModel(facilityLoadedModel.loadingAllPatients()),
            hasEffects(
                HideNoSearchResults,
                LoadAllPatients(facility)
            )
        ))
  }

  @Test
  fun `when search result is clicked, then open patient summary`() {
    val patientUuid = UUID.fromString("f607be71-630d-4adb-8d3a-76fdf347fe8a")
    val facility = TestData.facility(
        uuid = UUID.fromString("885c6339-9a96-4c8d-bfea-7eea74de6862"),
        name = "PHC Obvious"
    )
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
  fun `when the search result is clicked from the scanning bp passport flow, open the link id with patient screen`() {
    val patientUuid = UUID.fromString("f607be71-630d-4adb-8d3a-76fdf347fe8a")
    val identifier = TestData.identifier("123456", BpPassport)
    val facility = TestData.facility(
        uuid = UUID.fromString("658d2987-411e-47a4-97c6-84f8b0f072c0"),
        name = "PHC Obvious"
    )
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
  fun `when register new patient is clicked, then save ongoing patient entry`() {
    val facility = TestData.facility()
    val searchQueryModel = defaultModel
        .facilityLoaded(facility)
        .searchQueryChanged("Pat")

    val ongoingPatientEntry = OngoingNewPatientEntry.fromFullName("Pat")
        .withIdentifier(identifier)

    updateSpec
        .given(searchQueryModel)
        .whenEvent(RegisterNewPatientClicked)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(SaveNewOngoingPatientEntry(ongoingPatientEntry))
        ))
  }
}
