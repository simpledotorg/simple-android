package org.simple.clinic.login.applock

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.After
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.storage.MemoryValue
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import java.time.Instant
import java.util.Optional
import java.util.UUID

class AppLockEffectHandlerTest {

  private val uiActions = mock<AppLockUiActions>()
  private val lockAfterTimestampValue = MemoryValue(Optional.empty<Instant>())

  private val loggedInUser = TestData.loggedInUser(
      uuid = UUID.fromString("cdb08a78-7bae-44f4-9bb9-40257be58aa4"),
      pinDigest = "actual-hash"
  )
  private val facility = TestData.facility(
      uuid = UUID.fromString("33459993-53d0-4484-b8a9-66c8b065f07d"),
      name = "PHC Obvious"
  )

  private val effectHandler = AppLockEffectHandler(
      currentUser = { loggedInUser },
      currentFacility = { facility },
      schedulersProvider = TestSchedulersProvider.trampoline(),
      lockAfterTimestampValue = lockAfterTimestampValue,
      viewEffectsConsumer = AppLockViewEffectHandler(uiActions)::handle
  )
  private val testCase = EffectHandlerTestCase(effectHandler.build())

  @After
  fun tearDown() {
    testCase.dispose()
  }

  @Test
  fun `when exit app effect is received, then exit the app`() {
    // when
    testCase.dispatch(ExitApp)

    // then
    testCase.assertNoOutgoingEvents()

    verify(uiActions).exitApp()
    verifyNoMoreInteractions(uiActions)
  }
}
