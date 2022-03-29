package org.simple.clinic.home.patients

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.Test
import java.time.LocalDate

class PatientsTabUiRendererTest {
  private val ui = mock<PatientsTabUi>()
  private val uiRenderer = PatientsTabUiRenderer(ui, LocalDate.of(2022, 3, 28))
  private val defaultModel = PatientsTabModel.create()

  @Test
  fun `When app staleness is loaded and is present, then display app update nudge reason`() {
    //given
    val appStaleness = 75

    // when
    uiRenderer.render(defaultModel.updateAppStaleness(appStaleness))

    // then
    verify(ui).renderAppUpdateReason(appStalenessInMonths = 2)
    verifyNoMoreInteractions(ui)
  }
}
