package org.simple.clinic.summary.addcholesterol

import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import java.util.UUID

class CholesterolEntryUiRendererTest {

  private val ui = mock<CholesterolEntryUi>()
  private val uiRenderer = CholesterolEntryUiRenderer(ui)

  @Test
  fun `when cholesterol save state is saving, then show progress`() {
    // given
    val model = CholesterolEntryModel
        .create(
            patientUUID = UUID.fromString("e406d663-b71f-4367-b2b7-43f52e1eee1c")
        )
        .cholesterolChanged(cholesterolValue = 400f)
        .savingCholesterol()

    // when
    uiRenderer.render(model)

    // then
    verify(ui).showProgress()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when cholesterol save state is not saving, then hide progress`() {
    // given
    val model = CholesterolEntryModel
        .create(
            patientUUID = UUID.fromString("e406d663-b71f-4367-b2b7-43f52e1eee1c")
        )
        .cholesterolChanged(cholesterolValue = 400f)
        .cholesterolSaved()

    // when
    uiRenderer.render(model)

    // then
    verify(ui).hideProgress()
    verifyNoMoreInteractions(ui)
  }
}
