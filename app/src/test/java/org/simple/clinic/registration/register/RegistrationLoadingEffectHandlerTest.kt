package org.simple.clinic.registration.register

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import org.junit.After
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.security.pin.JavaHashPasswordHasher
import org.simple.clinic.user.User
import org.simple.clinic.user.UserStatus
import org.simple.clinic.user.registeruser.RegisterUser
import org.simple.clinic.util.TestUtcClock
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import java.time.Instant
import java.util.UUID

class RegistrationLoadingEffectHandlerTest {

  private val registerUser = mock<RegisterUser>()

  private val uiActions = mock<RegistrationLoadingUiActions>()

  private val currentFacility = TestData.facility(uuid = UUID.fromString("198963c5-4c45-4f02-903c-ada09d8f6877"))

  private val clock = TestUtcClock(Instant.parse("2018-01-01T00:00:00Z"))

  private val passwordHasher = JavaHashPasswordHasher()

  private val effectHandler = RegistrationLoadingEffectHandler(
      schedulers = TestSchedulersProvider.trampoline(),
      registerUser = registerUser,
      clock = clock,
      passwordHasher = passwordHasher,
      uiActions = uiActions
  ).build()

  private val testCase = EffectHandlerTestCase(effectHandler)

  @After
  fun tearDown() {
    testCase.dispose()
  }

  @Test
  fun `when the convert registration entry to user details effect is received, the registration entry must be converted to a user instance`() {
    // given
    val registrationEntry = TestData.ongoingRegistrationEntry(
        uuid = UUID.fromString("04ef663e-c50b-4a9c-b850-59d5460d168e"),
        phoneNumber = "1111111111",
        pin = "1111",
        registrationFacility = currentFacility,
        fullName = "Anish Acharya"
    )

    // when
    testCase.dispatch(ConvertRegistrationEntryToUserDetails(registrationEntry))

    // then
    val now = Instant.now(clock)
    val expectedUser = User(
        uuid = registrationEntry.uuid!!,
        fullName = registrationEntry.fullName!!,
        phoneNumber = registrationEntry.phoneNumber!!,
        pinDigest = passwordHasher.hash(registrationEntry.pin!!),
        status = UserStatus.WaitingForApproval,
        createdAt = now,
        updatedAt = now,
        loggedInStatus = User.LoggedInStatus.LOGGED_IN,
        registrationFacilityUuid = currentFacility.uuid,
        currentFacilityUuid = currentFacility.uuid,
        teleconsultPhoneNumber = null,
        capabilities = null
    )
    testCase.assertOutgoingEvents(ConvertedRegistrationEntryToUserDetails(expectedUser))
    verifyZeroInteractions(uiActions)
  }
}
