package org.simple.clinic.instantsearch

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.TestData
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
            hasEffects(SearchWithCriteria(PatientSearchCriteria.Name("Pat", identifier), facility))
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
            hasEffects(LoadAllPatients(facility))
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
  fun `when the search result is clicked from the scanning bp passport flow, open the link id with patient screen`() {
    val patientUuid = UUID.fromString("f607be71-630d-4adb-8d3a-76fdf347fe8a")
    val identifier = TestData.identifier("123456", BpPassport)
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
}
