package org.simple.clinic.instantsearch

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.Test

class InstantSearchUiRendererTest {

  private val ui = mock<InstantSearchUi>()
  private val uiRenderer = InstantSearchUiRenderer(ui)

  @Test
  fun `when the instant search progress state is in progress, then show search progress`() {
    // given
    val model = InstantSearchModel.create(null)
        .loadingSearchResults()

    // when
    uiRenderer.render(model)

    // then
    verify(ui).showSearchProgress()
    verifyNoMoreInteractions(ui)
  }
}
