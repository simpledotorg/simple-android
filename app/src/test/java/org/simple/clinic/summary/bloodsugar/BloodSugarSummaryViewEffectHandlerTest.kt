package org.simple.clinic.summary.bloodsugar

import org.junit.Test
import org.simple.clinic.mobius.EffectHandlerTestCase

class BloodSugarSummaryViewEffectHandlerTest {

  @Test
  fun `when fetch blood sugar effect is received then blood sugar should be fetched`() {
    //given
    val testCase = EffectHandlerTestCase(BloodSugarSummaryViewEffectHandler.create())

    //when
    testCase.dispatch(FetchBloodSugarSummary)

    //then
    testCase.assertOutgoingEvents(BloodSugarSummaryFetched)
  }
}
