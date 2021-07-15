package org.simple.clinic.drugs.search

import androidx.paging.PagingData
import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.TestData
import java.util.UUID

class DrugSearchUpdateTest {

  private val defaultModel = DrugSearchModel.create()
  private val updateSpec = UpdateSpec(DrugSearchUpdate())

  @Test
  fun `when drugs search results are loaded, then show drug search results`() {
    val searchResults = PagingData.from(listOf(
        TestData.drug(id = UUID.fromString("6604240d-c83d-4476-978f-06af96750719"),
            name = "Amlodipine",
            dosage = "10mg"),
        TestData.drug(id = UUID.fromString("e700ab22-e5fa-4037-a139-8bd678bbf035"),
            name = "Amlodipine",
            dosage = "20mg")
    ))

    updateSpec
        .given(defaultModel)
        .whenEvent(DrugsSearchResultsLoaded(searchResults))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(SetDrugsSearchResults(searchResults))
        ))
  }

  @Test
  fun `when search query is changed, then update model and search drugs`() {
    val searchQuery = "Amlodipine"

    updateSpec
        .given(defaultModel)
        .whenEvent(SearchQueryChanged(searchQuery))
        .then(assertThatNext(
            hasModel(defaultModel.searchQueryChanged(searchQuery)),
            hasEffects(SearchDrugs(searchQuery))
        ))
  }

  @Test
  fun `when search query is empty, then update model`() {
    val searchQuery = ""

    updateSpec
        .given(defaultModel)
        .whenEvent(SearchQueryChanged(searchQuery))
        .then(assertThatNext(
            hasModel(defaultModel.searchQueryChanged(searchQuery)),
            hasNoEffects()
        ))
  }
}
