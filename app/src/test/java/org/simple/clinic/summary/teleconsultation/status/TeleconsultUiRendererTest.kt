package org.simple.clinic.summary.teleconsultation.status

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.Test
import org.simple.clinic.teleconsultlog.teleconsultrecord.TeleconsultStatus

class TeleconsultUiRendererTest {

  @Test
  fun `when teleconsult status is present, then enable done button`() {
    // given
    val ui = mock<TeleconsultStatusUi>()
    val uiRenderer = TeleconsultStatusUiRenderer(ui)

    val model = TeleconsultStatusModel.create()
        .teleconsultStatusChanged(TeleconsultStatus.Yes)

    // when
    uiRenderer.render(model)

    // then
    verify(ui).enableDoneButton()
    verifyNoMoreInteractions(ui)
  }
}
