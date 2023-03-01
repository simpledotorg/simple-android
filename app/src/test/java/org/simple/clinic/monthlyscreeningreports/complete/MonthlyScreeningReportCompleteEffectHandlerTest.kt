package org.simple.clinic.monthlyscreeningreports.complete

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.After
import org.junit.Test
import org.simple.clinic.mobius.EffectHandlerTestCase

class MonthlyScreeningReportCompleteEffectHandlerTest {

  private val ui = mock<MonthlyScreeningReportCompleteUi>()
  private val viewEffectHandler = MonthlyScreeningReportCompleteViewEffectHandler(ui)

  private val effectHandler = MonthlyScreeningReportCompleteEffectHandler(
      viewEffectsConsumer = viewEffectHandler::handle
  )

  private val testCase = EffectHandlerTestCase(effectHandler.build())

  @After
  fun tearDown() {
    testCase.dispose()
  }

  @Test
  fun `when go to monthly screening report list effect is received, then go to the same`() {
    // when
    testCase.dispatch(GoToMonthlyReportListScreen)

    // then
    testCase.assertNoOutgoingEvents()

    verify(ui).goToMonthlyReportListScreen()
    verifyNoMoreInteractions(ui)
  }
}
