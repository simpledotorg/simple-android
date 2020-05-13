package org.simple.clinic.deeplink

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import dagger.Lazy
import org.junit.After
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.scheduler.TrampolineSchedulersProvider
import java.util.UUID

class DeepLinkEffectHandlerTest {

  private val userSession = mock<UserSession>()

  private val effectHandler = DeepLinkEffectHandler(
      userSession = Lazy { userSession },
      schedulerProvider = TrampolineSchedulersProvider()
  ).build()
  private val testCase = EffectHandlerTestCase(effectHandler)

  @After
  fun tearDown() {
    testCase.dispose()
  }

  @Test
  fun `when fetch user effect is received, then fetch the user`() {
    // given
    val user = TestData.loggedInUser(
        uuid = UUID.fromString("ee59391d-ede9-46ef-8d03-ef54125fbf86")
    )
    whenever(userSession.loggedInUserImmediate()) doReturn user

    // when
    testCase.dispatch(FetchUser)

    // then
    testCase.assertOutgoingEvents(UserFetched(user))
  }
}
