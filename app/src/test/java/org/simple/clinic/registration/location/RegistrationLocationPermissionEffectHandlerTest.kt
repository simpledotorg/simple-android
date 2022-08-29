package org.simple.clinic.registration.location

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.After
import org.junit.Test
import org.simple.sharedTestCode.TestData
import org.simple.clinic.mobius.EffectHandlerTestCase
import java.util.UUID

class RegistrationLocationPermissionEffectHandlerTest {
  private val uiActions = mock<RegistrationLocationPermissionUiActions>()
  private val effectHandler = RegistrationLocationPermissionEffectHandler(
      viewEffectConsumer = RegistrationLocationPermissionViewEffectHandler(uiActions)::handle
  ).build()
  private val testCase = EffectHandlerTestCase(effectHandler)

  @After
  fun tearDown() {
    testCase.dispose()
  }

  @Test
  fun `when open facility selection screen, then open facility selection screen`() {
    // given
    val ongoingRegistrationEntry = TestData.ongoingRegistrationEntry(uuid = UUID.fromString("860d4587-df76-46cc-ac7c-97a29b2e2bf3"))

    // when
    testCase.dispatch(OpenFacilitySelectionScreen(ongoingRegistrationEntry))

    // then
    verify(uiActions).openFacilitySelectionScreen(ongoingRegistrationEntry)
    verifyNoMoreInteractions(uiActions)
    testCase.assertNoOutgoingEvents()
  }
}
