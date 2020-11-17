package org.simple.clinic.registration.register

import com.nhaarman.mockitokotlin2.mock
import org.junit.After
import org.simple.clinic.TestData
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.user.registeruser.RegisterUser
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import java.util.UUID

class RegistrationLoadingEffectHandlerTest {

  private val registerUser = mock<RegisterUser>()

  private val uiActions = mock<RegistrationLoadingUiActions>()

  private val currentFacility = TestData.facility(uuid = UUID.fromString("198963c5-4c45-4f02-903c-ada09d8f6877"))

  private val currentUser = TestData.loggedInUser(
      uuid = UUID.fromString("de930d5c-2d13-4f19-a194-b3679d543e7d"),
      registrationFacilityUuid = currentFacility.uuid,
      currentFacilityUuid = currentFacility.uuid
  )

  private val effectHandler = RegistrationLoadingEffectHandler(
      schedulers = TestSchedulersProvider.trampoline(),
      registerUser = registerUser,
      currentUser = { currentUser },
      currentFacility = { currentFacility },
      uiActions = uiActions
  ).build()

  private val testCase = EffectHandlerTestCase(effectHandler)

  @After
  fun tearDown() {
    testCase.dispose()
  }
}
