package org.simple.clinic.instantsearch

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.patient.PatientSearchCriteria

class InstantSearchUpdateTest {

  private val updateSpec = UpdateSpec(InstantSearchUpdate())
  private val defaultModel = InstantSearchModel.create()

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
            hasEffects(SearchWithCriteria(PatientSearchCriteria.Name("Pat"), facility))
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
}
