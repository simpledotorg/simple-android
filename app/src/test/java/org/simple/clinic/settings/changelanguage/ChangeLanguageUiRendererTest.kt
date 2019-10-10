package org.simple.clinic.settings.changelanguage

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verifyZeroInteractions
import org.junit.Test

class ChangeLanguageUiRendererTest {

  private val ui = mock<ChangeLanguageUi>()
  private val defaultModel = ChangeLanguageModel.FETCHING_LANGUAGES

  private val renderer = ChangeLanguageUiRenderer(ui)

  @Test
  fun `when the model is being initialized, do nothing`() {
    // when
    renderer.render(defaultModel)

    // then
    verifyZeroInteractions(ui)
  }
}
