package org.simple.clinic.summary.bloodpressures.newbpsummary

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verifyZeroInteractions
import org.junit.Test
import java.util.UUID

class NewBloodPressureSummaryViewUiRendererTest {
  @Test
  fun `when blood pressures are loading, then do nothing`() {
    // given
    val patientUuid = UUID.fromString("8b298cc4-da11-4df9-a318-01e113f3abe3")
    val ui = mock<NewBloodPressureSummaryViewUi>()
    val uiRenderer = NewBloodPressureSummaryViewUiRenderer(ui)

    // when
    uiRenderer.render(NewBloodPressureSummaryViewModel.create(patientUuid))

    // then
    verifyZeroInteractions(ui)
  }
}
