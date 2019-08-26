package org.simple.clinic.analytics

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Observable
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
import org.simple.clinic.util.scheduler.TrampolineSchedulersProvider

@RunWith(JUnitParamsRunner::class)
class UpdateAnalyticsUserIdTest {

  private val reporter = MockAnalyticsReporter()
  private val userSession = mock<UserSession>()
  private val updateAnalyticsUserId = UpdateAnalyticsUserId(userSession, TrampolineSchedulersProvider())

  @Before
  fun setUp() {
    Analytics.addReporter(reporter)
  }

  @After
  fun tearDown() {
    Analytics.clearReporters()
    reporter.clear()
  }

  @Test
  fun `when there is no logged in user present, the user id must not be set`() {
    // given
    whenever(userSession.loggedInUser()).thenReturn(Observable.just(None))

    // when
    updateAnalyticsUserId.listen()

    // then
    assertThat(reporter.user).isNull()
  }

  @Test
  @Parameters(value = [
    "RESETTING_PIN",
    "RESET_PIN_REQUESTED",
    "LOGGED_IN"
  ])
  fun `the user id must be set only if the local logged in status is LOGGED_IN, RESETTING_PIN or RESET_PIN_REQUESTED`(
      loggedInStatus: User.LoggedInStatus
  ) {
    // given
    val user = PatientMocker.loggedInUser(loggedInStatus = loggedInStatus)
    whenever(userSession.loggedInUser()).thenReturn(Observable.just(Just(user)))

    // when
    updateAnalyticsUserId.listen()

    // then
    assertThat(reporter.user).isEqualTo(user)
  }

  @Test
  @Parameters(value = [
    "NOT_LOGGED_IN",
    "OTP_REQUESTED",
    "UNAUTHORIZED"
  ])
  fun `the user id must not be set if the local logged in status is NOT_LOGGED_IN, OTP_REQUESTED or UNAUTHORIZED`(
      loggedInStatus: User.LoggedInStatus
  ) {
    // given
    val user = PatientMocker.loggedInUser(loggedInStatus = loggedInStatus)
    whenever(userSession.loggedInUser()).thenReturn(Observable.just(Just(user)))

    // when
    updateAnalyticsUserId.listen()

    // then
    assertThat(reporter.user).isNull()
  }
}
