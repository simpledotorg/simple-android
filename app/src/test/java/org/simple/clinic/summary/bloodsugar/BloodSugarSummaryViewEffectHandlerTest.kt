package org.simple.clinic.summary.bloodsugar

import org.junit.Test
import org.simple.clinic.mobius.EffectHandlerTestCase
import java.util.UUID

class BloodSugarSummaryViewEffectHandlerTest {

  @Test
  fun `when fetch blood sugar effect is received then blood sugar should be fetched`() {
    //given
    val testCase = EffectHandlerTestCase(BloodSugarSummaryViewEffectHandler.create())

    //when
    testCase.dispatch(FetchBloodSugarSummary(UUID.fromString("69cdea01-fbd8-437a-844c-25e412f32a9e")))

    //then
    testCase.assertOutgoingEvents(BloodSugarSummaryFetched)
  }
}
