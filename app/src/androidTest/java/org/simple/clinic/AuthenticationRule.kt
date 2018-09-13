package org.simple.clinic

import com.google.common.truth.Truth.assertThat
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import org.simple.clinic.login.LoginResult
import org.simple.clinic.user.User
import org.simple.clinic.user.UserSession
import org.simple.clinic.user.UserStatus
import javax.inject.Inject

/** Runs every test with an authenticated user. */
class AuthenticationRule : TestRule {

  @Inject
  lateinit var userSession: UserSession

  @Inject
  lateinit var testData: TestData

  override fun apply(base: Statement, description: Description): Statement {
    return object : Statement() {
      override fun evaluate() {
        TestClinicApp.appComponent().inject(this@AuthenticationRule)

        // Cannot figure out why, but we're occasionally seeing failed tests
        // because facility syncing gets called with stale "last-pull" timestamp.
        // As a workaround, the app data will now be cleared before running
        // every test and not after.
        logout()

        try {
          // Login also needs to happen inside this try block so that in case
          // of a failure, logout() still gets called to reset all app data.
          login()
          base.evaluate()

        } finally {
          logout()
        }
      }
    }
  }

  private fun login() {
    val loginResult = userSession.saveOngoingLoginEntry(testData.qaOngoingLoginEntry())
        .andThen(userSession.loginWithOtp(testData.qaUserOtp()))
        .blockingGet()
    assertThat(loginResult).isInstanceOf(LoginResult.Success::class.java)

    val (accessToken) = userSession.accessToken()
    assertThat(accessToken).isNotNull()

    val (loggedInUser) = userSession.loggedInUser().blockingFirst()
    assertThat(userSession.isUserLoggedIn()).isTrue()
    assertThat(loggedInUser!!.status).isEqualTo(UserStatus.APPROVED_FOR_SYNCING)
    assertThat(loggedInUser.loggedInStatus).isEqualTo(User.LoggedInStatus.LOGGED_IN)
  }

  private fun logout() {
    userSession.logout().blockingAwait()
  }
}
