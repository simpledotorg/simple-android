package org.simple.clinic.security.pin

import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.junit.After
import org.junit.Test
import org.simple.clinic.DEMO_FACILITY
import org.simple.clinic.DEMO_FACILITY_ID
import org.simple.clinic.DEMO_USER_ID
import org.simple.clinic.DEMO_USER_PHONE_NUMBER
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.user.LoggedInUserPayload
import org.simple.clinic.user.User
import org.simple.clinic.user.UserStatus
import org.simple.clinic.util.scheduler.TrampolineSchedulersProvider
import java.time.Instant

class PinEntryEffectHandlerTest {

  private val uiActions = mock<UiActions>()
  private val facilityRepository = mock<FacilityRepository>()

  private val effectHandler = PinEntryEffectHandler(
      bruteForceProtection = mock(),
      schedulersProvider = TrampolineSchedulersProvider(),
      uiActions = uiActions,
      pinVerificationMethod = mock(),
      facilityRepository = facilityRepository
  )

  private val testCase = EffectHandlerTestCase(effectHandler.build())

  @After
  fun tearDown() {
    testCase.dispose()
  }

  @Test
  fun `when the show network error effect is received, the network error must be shown`() {
    // when
    testCase.dispatch(ShowNetworkError)

    // then
    testCase.assertNoOutgoingEvents()
    verify(uiActions).showNetworkError()
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when the show server error effect is received, the server error must be shown`() {
    // when
    testCase.dispatch(ShowServerError)

    // then
    testCase.assertNoOutgoingEvents()
    verify(uiActions).showServerError()
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when the show unexpected error effect is received, the generic error must be shown`() {
    // when
    testCase.dispatch(ShowUnexpectedError)

    // then
    testCase.assertNoOutgoingEvents()
    verify(uiActions).showUnexpectedError()
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when save demo facility effect is received, then save demo facility`() {
    // given
    val userPayload = LoggedInUserPayload(
        uuid = DEMO_USER_ID,
        fullName = "Demo User",
        phoneNumber = DEMO_USER_PHONE_NUMBER,
        pinDigest = "pin-digest",
        registrationFacilityId = DEMO_FACILITY_ID,
        status = UserStatus.WaitingForApproval,
        createdAt = Instant.parse("2018-01-01T00:00:00Z"),
        updatedAt = Instant.parse("2018-01-01T00:00:00Z"),
        teleconsultPhoneNumber = null,
        capabilities = User.Capabilities(canTeleconsult = User.CapabilityStatus.No)
    )

    // when
    testCase.dispatch(SaveDemoFacility(userPayload))

    // then
    verify(facilityRepository).save(listOf(DEMO_FACILITY))
    verifyNoMoreInteractions(facilityRepository)

    testCase.assertOutgoingEvents(DemoFacilitySaved(userPayload))
  }
}
