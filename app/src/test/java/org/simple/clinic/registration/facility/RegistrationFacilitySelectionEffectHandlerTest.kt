package org.simple.clinic.registration.facility

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.After
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import java.util.UUID

class RegistrationFacilitySelectionEffectHandlerTest {
  private val uiActions = mock<RegistrationFacilitySelectionUiActions>()
  private val effectHandler = RegistrationFacilitySelectionEffectHandler(
      schedulersProvider = TestSchedulersProvider.trampoline(),
      uiActions = uiActions,
      viewEffectsConsumer = RegistrationFacilitySelectionViewEffectHandler(uiActions)::handle
  ).build()
  private val testCase = EffectHandlerTestCase(effectHandler)

  @After
  fun tearDown() {
    testCase.dispose()
  }

  @Test
  fun `when open confirm facility sheet effect is received, then show confirm facility sheet`() {
    // given
    val facilityUuid = UUID.fromString("ab51824f-d956-4e5a-a01f-dd46a99a5c3b")
    val facilityName = "CHC Bhagta"
    val facility = TestData.facility(uuid = facilityUuid, name = facilityName)

    // when
    testCase.dispatch(OpenConfirmFacilitySheet(facility))

    // then
    verify(uiActions).showConfirmFacilitySheet(facilityUuid, facilityName)
    verifyNoMoreInteractions(uiActions)
    testCase.assertNoOutgoingEvents()
  }

  @Test
  fun `when move to intro video screen effect is received, then open intro video screen`() {
    // given
    val ongoingRegistrationEntry = TestData.ongoingRegistrationEntry(uuid = UUID.fromString("79ae24ef-6ca2-4380-882d-050b271928da"))

    // when
    testCase.dispatch(MoveToIntroVideoScreen(ongoingRegistrationEntry))

    // then
    verify(uiActions).openIntroVideoScreen(ongoingRegistrationEntry)
    verifyNoMoreInteractions(uiActions)
    testCase.assertNoOutgoingEvents()
  }
}
