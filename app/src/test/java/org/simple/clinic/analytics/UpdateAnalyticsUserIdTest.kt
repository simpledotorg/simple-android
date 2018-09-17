package org.simple.clinic.analytics

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.user.User
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.Just
import org.simple.clinic.util.None
import org.simple.clinic.util.Optional

@RunWith(JUnitParamsRunner::class)
class UpdateAnalyticsUserIdTest {

  private val reporter = MockReporter()
  private val userSession = mock<UserSession>()
  private val updateAnalyticsUserId = UpdateAnalyticsUserId(userSession)
  private val scheduler = Schedulers.trampoline()

  @Before
  fun setUp() {
    Analytics.addReporter(reporter)
  }

  @Test
  fun `when there is no logged in user present, the user id must not be set`() {
    whenever(userSession.loggedInUser()).thenReturn(Observable.just(None))

    updateAnalyticsUserId.listen(scheduler)

    assertThat(reporter.setUserIds).isEmpty()
  }

  @Test
  @Parameters(value = [
    "NOT_LOGGED_IN|false",
    "OTP_REQUESTED|false",
    "RESETTING_PIN|true",
    "RESET_PIN_REQUESTED|true",
    "LOGGED_IN|true"
  ])
  fun `the user id must be set only if the local logged in status is LOGGED_IN, RESETTING_PIN or RESET_PIN_REQUESTED`(
      loggedInStatus: User.LoggedInStatus,
      shouldSetUserId: Boolean
  ) {
    val user = PatientMocker.loggedInUser(loggedInStatus = loggedInStatus)
    whenever(userSession.loggedInUser()).thenReturn(Observable.just(Just(user)))

    updateAnalyticsUserId.listen(scheduler)

    if (shouldSetUserId) {
      assertThat(reporter.setUserIds).isEqualTo(listOf(user.uuid.toString()))
    } else {
      assertThat(reporter.setUserIds).isEmpty()
    }
  }

  @Test
  @Parameters(method = "paramsForUpdatedUser")
  fun `the user id must be set when the local user is updated to one that is logged in`(
      previousUser: Optional<User>,
      updatedUser: Optional<User>,
      userIdThatMustBeSet: String
  ) {
    whenever(userSession.loggedInUser()).thenReturn(Observable.just(previousUser, updatedUser))

    updateAnalyticsUserId.listen(scheduler)

    assertThat(reporter.setUserIds).isEqualTo(listOf(userIdThatMustBeSet))
  }

  // Accessed via reflection for test params
  @Suppress("Unused")
  fun paramsForUpdatedUser(): Array<Array<Any>> {
    val user1 = PatientMocker.loggedInUser(loggedInStatus = User.LoggedInStatus.LOGGED_IN)
    val user2 = PatientMocker.loggedInUser(loggedInStatus = User.LoggedInStatus.NOT_LOGGED_IN)
    return arrayOf(
        arrayOf(None, Just(user1), user1.uuid.toString()),
        arrayOf(Just(user2), Just(user2.copy(loggedInStatus = User.LoggedInStatus.LOGGED_IN)), user2.uuid.toString())
    )
  }

  @After
  fun tearDown() {
    Analytics.clearReporters()
    reporter.clear()
  }
}
