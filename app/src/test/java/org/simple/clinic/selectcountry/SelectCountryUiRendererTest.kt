package org.simple.clinic.selectcountry

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import org.junit.Test

class SelectCountryUiRendererTest {

  @Test
  fun `when the model is being initialized, show the progress bar`() {
    // given
    val model = SelectCountryModel.FETCHING
    val ui = mock<SelectCountryUi>()
    val renderer = SelectCountryUiRenderer(ui)

    // when
    renderer.render(model)

    // then
    verify(ui).showProgress()
    verifyNoMoreInteractions(ui)
  }
}
