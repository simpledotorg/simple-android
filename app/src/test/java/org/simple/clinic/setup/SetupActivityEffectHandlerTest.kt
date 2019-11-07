package org.simple.clinic.setup

import com.f2prateek.rx.preferences2.Preference
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import com.nhaarman.mockito_kotlin.verifyZeroInteractions
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Single
import org.junit.After
import org.junit.Test
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.user.User
import org.simple.clinic.util.Just
import org.simple.clinic.util.None
import org.simple.clinic.util.scheduler.TrampolineSchedulersProvider
import java.util.UUID

class SetupActivityEffectHandlerTest {

  private val onboardingCompletePreference = mock<Preference<Boolean>>()
  private val uiActions = mock<UiActions>()
  private val userDao = mock<User.RoomDao>()

  private val effectHandler = SetupActivityEffectHandler.create(
      onboardingCompletePreference,
      uiActions,
      userDao,
      TrampolineSchedulersProvider()
  )
  private val testCase = EffectHandlerTestCase(effectHandler)

  @After
  fun tearDown() {
    testCase.dispose()
  }

  @Test
  fun `the user onboarding status must be fetched when the fetch user details effect is received`() {
    // given
    whenever(onboardingCompletePreference.get()) doReturn true
    whenever(userDao.userImmediate()).doReturn<User?>(null)

    // when
    testCase.dispatch(FetchUserDetails)

    // then
    testCase.assertOutgoingEvents(UserDetailsFetched(hasUserCompletedOnboarding = true, loggedInUser = None))
    verifyZeroInteractions(uiActions)
  }

  @Test
  fun `when the go to main activity effect is received, the main activity must be opened`() {
    // when
    testCase.dispatch(GoToMainActivity)

    // then
    testCase.assertNoOutgoingEvents()
    verify(uiActions).goToMainActivity()
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when the show onboarding screen effect is received, the onboarding screen must be shown`() {
    // when
    testCase.dispatch(ShowOnboardingScreen)

    // then
    testCase.assertNoOutgoingEvents()
    verify(uiActions).showOnboardingScreen()
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when the initialize database screen effect is received, the database must be initialized`() {
    // given
    whenever(userDao.userCount()) doReturn Single.just(0)

    // when
    testCase.dispatch(InitializeDatabase)

    // then
    testCase.assertOutgoingEvents(DatabaseInitialized)
    verifyZeroInteractions(uiActions)
  }

  @Test
  fun `the logged in user must be fetched when the fetch user details effect is received`() {
    // given
    whenever(onboardingCompletePreference.get()) doReturn false
    val user = PatientMocker.loggedInUser(uuid = UUID.fromString("426d2eb9-ebf7-4a62-b157-1de221c7c3d0"))
    whenever(userDao.userImmediate()).doReturn(user)

    // when
    testCase.dispatch(FetchUserDetails)

    // then
    val expected = UserDetailsFetched(hasUserCompletedOnboarding = false, loggedInUser = Just(user))
    testCase.assertOutgoingEvents(expected)
    verifyZeroInteractions(uiActions)
  }
}
