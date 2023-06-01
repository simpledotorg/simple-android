package org.simple.clinic.registration.facility

import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.junit.After
import org.junit.Test
import org.simple.sharedTestCode.TestData
import org.simple.clinic.mobius.EffectHandlerTestCase
import java.util.UUID

class RegistrationFacilitySelectionEffectHandlerTest {
  private val uiActions = mock<RegistrationFacilitySelectionUiActions>()
  private val effectHandler = RegistrationFacilitySelectionEffectHandler(
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

  @Test
  fun `when move to registration loading screen effect is received, then move to registration loading screen`() {
    // given
    val ongoingRegistrationEntry = TestData.ongoingRegistrationEntry(uuid = UUID.fromString("79ae24ef-6ca2-4380-882d-050b271928da"))

    // when
    testCase.dispatch(MoveToRegistrationLoadingScreen(ongoingRegistrationEntry))

    // then
    verify(uiActions).openRegistrationLoadingScreen(ongoingRegistrationEntry)
    verifyNoMoreInteractions(uiActions)
    testCase.assertNoOutgoingEvents()
  }
}
