package org.simple.clinic.summary.teleconsultation.status

import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.junit.Test
import org.simple.clinic.teleconsultlog.teleconsultrecord.TeleconsultStatus
import java.util.UUID

class TeleconsultUiRendererTest {

  @Test
  fun `when teleconsult status is present, then enable done button`() {
    // given
    val ui = mock<TeleconsultStatusUi>()
    val uiRenderer = TeleconsultStatusUiRenderer(ui)

    val model = TeleconsultStatusModel.create(
        teleconsultRecordId = UUID.fromString("f5387bb5-0dbf-4d91-a487-bbe219adcd60")
    ).teleconsultStatusChanged(TeleconsultStatus.Yes)

    // when
    uiRenderer.render(model)

    // then
    verify(ui).enableDoneButton()
    verifyNoMoreInteractions(ui)
  }
}
