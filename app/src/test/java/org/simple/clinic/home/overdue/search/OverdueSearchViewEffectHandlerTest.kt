package org.simple.clinic.home.overdue.search

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.Test
import java.util.UUID

class OverdueSearchViewEffectHandlerTest {

  private val uiActions = mock<OverdueSearchUiActions>()
  private val viewEffectHandler = OverdueSearchViewEffectHandler(uiActions)

  @Test
  fun `when open patient summary view effect is received, then open patient summary screen`() {
    // given
    val patientUuid = UUID.fromString("fc831110-5da8-4bea-9036-5b5d6334dc1a")

    // when
    viewEffectHandler.handle(OpenPatientSummary(patientUuid))

    // then
    verify(uiActions).openPatientSummaryScreen(patientUuid)
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when call overdue patient view effect is received, then open contact patient sheet`() {
    // given
    val patientUuid = UUID.fromString("fc831110-5da8-4bea-9036-5b5d6334dc1a")

    // when
    viewEffectHandler.handle(OpenContactPatientSheet(patientUuid))

    // then
    verify(uiActions).openContactPatientSheet(patientUuid)
    verifyNoMoreInteractions(uiActions)
  }
}
