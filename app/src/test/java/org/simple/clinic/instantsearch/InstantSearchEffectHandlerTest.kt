package org.simple.clinic.instantsearch

import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.util.scheduler.TestSchedulersProvider

class InstantSearchEffectHandlerTest {

  @Test
  fun `when load current facility effect is received, then load the current facility`() {
    // given
    val facility = TestData.facility()
    val effectHandler = InstantSearchEffectHandler(
        currentFacility = { facility },
        schedulers = TestSchedulersProvider.trampoline()
    ).build()
    val testCase = EffectHandlerTestCase(effectHandler)

    // when
    testCase.dispatch(LoadCurrentFacility)

    // then
    testCase.assertOutgoingEvents(CurrentFacilityLoaded(facility))
    testCase.dispose()
  }
}
