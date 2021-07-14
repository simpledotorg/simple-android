package org.simple.clinic.drugs.search

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.Test

class DrugSearchUiRendererTest {

  private val ui = mock<DrugSearchUi>()
  private val uiRenderer = DrugSearchUiRenderer(ui)

  private val defaultModel = DrugSearchModel.create()

  @Test
  fun `when search query is empty, then hide search results`() {
    // given
    val emptySearchQueryModel = defaultModel
        .searchQueryChanged(searchQuery = "")

    // when
    uiRenderer.render(emptySearchQueryModel)

    // then
    verify(ui).hideSearchResults()
    verifyNoMoreInteractions(ui)
  }
}
