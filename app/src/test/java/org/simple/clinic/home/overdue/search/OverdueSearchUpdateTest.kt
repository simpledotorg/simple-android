package org.simple.clinic.home.overdue.search

import androidx.paging.PagingData
import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.home.overdue.search.OverdueSearchProgressState.DONE
import org.simple.clinic.home.overdue.search.OverdueSearchProgressState.IN_PROGRESS
import org.simple.clinic.home.overdue.search.OverdueSearchQueryValidator.Result.Valid
import org.simple.sharedTestCode.TestData
import java.time.LocalDate
import java.util.UUID

class OverdueSearchUpdateTest {

  private val date = LocalDate.of(2022, 2, 23)
  private val updateSpec = UpdateSpec(OverdueSearchUpdate(date))
  private val defaultModel = OverdueSearchModel.create()

  @Test
  fun `when overdue search history is loaded, then update the model`() {
    val searchHistory = setOf(
        "Babri",
        "Ramesh"
    )

    updateSpec
        .given(defaultModel)
        .whenEvent(OverdueSearchHistoryLoaded(searchHistory))
        .then(assertThatNext(
            hasModel(defaultModel.overdueSearchHistoryLoaded(searchHistory)),
            hasNoEffects()
        ))
  }

  @Test
  fun `when overdue search query is changed, then validate the search query`() {
    val searchQuery = "Babri"
    updateSpec
        .given(defaultModel)
        .whenEvent(OverdueSearchQueryChanged(searchQuery))
        .then(assertThatNext(
            hasModel(defaultModel.overdueSearchQueryChanged(searchQuery)),
            hasEffects(ValidateOverdueSearchQuery(searchQuery))
        ))
  }

  @Test
  fun `when search query is validated and is valid, then update model, add query to search history and search overdue patients`() {
    val searchQuery = "Babri"
    updateSpec
        .given(defaultModel)
        .whenEvent(OverdueSearchQueryValidated(Valid(searchQuery)))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(AddQueryToOverdueSearchHistory(searchQuery), SearchOverduePatients(searchQuery, date))
        ))
  }

  @Test
  fun `when overdue search results are loaded, then show overdue search results`() {
    val searchQuery = "Babri"
    val facilityUuid = UUID.fromString("7dba16a0-1090-41f6-8e0c-0d97989de898")
    val overdueAppointments = listOf(TestData.overdueAppointment(
        facilityUuid = facilityUuid,
        name = "Anish Acharya",
        patientUuid = UUID.fromString("37259e96-e757-4608-aeae-f1a20b088f09")
    ), TestData.overdueAppointment(
        facilityUuid = facilityUuid,
        name = "Anirban Dar",
        patientUuid = UUID.fromString("53659148-a157-4aa4-92fb-c0a7991ae872")
    ))

    val overdueSearchResult = PagingData.from(overdueAppointments)

    updateSpec
        .given(defaultModel.overdueSearchQueryChanged(searchQuery))
        .whenEvent(OverdueSearchResultsLoaded(overdueSearchResult))
        .then(
            assertThatNext(
                hasNoModel(),
                hasEffects(ShowOverdueSearchResults(overdueSearchResult, searchQuery))
            )
        )
  }

  @Test
  fun `when overdue patient is clicked, then open patient summary`() {
    val patientUuid = UUID.fromString("4831397d-4314-49fb-9ec7-3ae0a70c25c1")
    updateSpec
        .given(defaultModel)
        .whenEvent(OverduePatientClicked(patientUuid))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(OpenPatientSummary(patientUuid))
        ))
  }

  @Test
  fun `when call overdue patient is clicked, then open contact patient sheet`() {
    val patientUuid = UUID.fromString("4831397d-4314-49fb-9ec7-3ae0a70c25c1")
    updateSpec
        .given(defaultModel)
        .whenEvent(CallPatientClicked(patientUuid))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(OpenContactPatientSheet(patientUuid))
        ))
  }

  @Test
  fun `when search history item is clicked, then set overdue search query`() {
    val searchQuery = "Babri"
    updateSpec
        .given(defaultModel)
        .whenEvent(OverdueSearchHistoryClicked(searchQuery))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(SetOverdueSearchQuery("Babri"))
        ))
  }

  @Test
  fun `when load state is changed, then update the model`() {
    val loadStateChangedModel = defaultModel
        .loadStateChanged(DONE)

    updateSpec
        .given(loadStateChangedModel)
        .whenEvent(OverdueSearchLoadStateChanged(IN_PROGRESS))
        .then(assertThatNext(
            hasModel(loadStateChangedModel.loadStateChanged(IN_PROGRESS)),
            hasNoEffects()
        ))
  }

  @Test
  fun `when overdue search screen is shown and search query is present, then set overdue search query`() {
    val searchQueryModel = defaultModel
        .overdueSearchQueryChanged("Babri")

    updateSpec
        .given(searchQueryModel)
        .whenEvent(OverdueSearchScreenShown)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(SetOverdueSearchQuery("Babri"))
        ))
  }
}
