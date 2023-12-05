package org.simple.clinic.login.applock

import com.f2prateek.rx.preferences2.Preference
import org.junit.After
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.storage.MemoryValue
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import org.simple.sharedTestCode.TestData
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
  private val hasUserConsentedToDataProtectionPreference = mock<Preference<Boolean>>()

  private val effectHandler = AppLockEffectHandler(
      currentUser = { loggedInUser },
      currentFacility = { facility },
      hasUserConsentedToDataProtectionPreference = hasUserConsentedToDataProtectionPreference,
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

  @Test
  fun `when load data protection consent effect is received, then load the data protection consent`() {
    // given
    whenever(hasUserConsentedToDataProtectionPreference.get()) doReturn true

    // when
    testCase.dispatch(LoadDataProtectionConsent)

    // then
    testCase.assertOutgoingEvents(DataProtectionConsentLoaded(hasUserConsentedToDataProtection = true))
  }

  @Test
  fun `when show data protection consent effect is received, then show the data protection dialog`() {
    // when
    testCase.dispatch(ShowDataProtectionConsentDialog)

    // then
    testCase.assertNoOutgoingEvents()

    verify(uiActions).showDataProtectionConsentDialog()
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when mark data protection consent effect is received, then mark data protection consent`() {
    // when
    testCase.dispatch(MarkDataProtectionConsent)

    // then
    testCase.assertOutgoingEvents(FinishedMarkingDataProtectionConsent)

    verifyNoInteractions(uiActions)
  }
}
