package org.simple.clinic.login

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.simple.clinic.TestClinicApp
import org.simple.clinic.login.activateuser.ActivateUserRequest
import org.simple.clinic.rules.ServerAuthenticationRule
import org.simple.clinic.user.User
import org.simple.clinic.user.UserSession
import org.simple.clinic.user.UserStatus
import org.simple.clinic.util.Rules
import javax.inject.Inject
import javax.inject.Named


class LoginUserWithOtpServerIntegrationTest {

  @get:Rule
  val ruleChain: RuleChain = Rules
      .global()
      .around(ServerAuthenticationRule())

  @Inject
  lateinit var userSession: UserSession

  @Inject
  lateinit var loginUserWithOtp: LoginUserWithOtp

  @Inject
  lateinit var usersApi: UsersApi

  @Inject
  @Named("user_pin")
  lateinit var userPin: String

  @Inject
  @Named("user_otp")
  lateinit var userOtp: String

  @Before
  fun setUp() {
    TestClinicApp.appComponent().inject(this)
  }

  @Test
  fun when_correct_login_params_are_given_then_login_should_happen_and_session_data_should_be_persisted() {
    val user = userSession.loggedInUserImmediate()!!

    usersApi.activate(ActivateUserRequest.create(user.uuid, userPin)).execute()

    val loginResult = loginUserWithOtp.loginWithOtp(user.phoneNumber, userPin, userOtp)
        .blockingGet()

    assertThat(loginResult).isInstanceOf(LoginResult.Success::class.java)
    val (accessToken) = userSession.accessToken()
    assertThat(accessToken).isNotNull()

    val (loggedInUser) = userSession.loggedInUser().blockingFirst()
    assertThat(userSession.isUserPresentLocally()).isTrue()
    assertThat(loggedInUser!!.status).isEqualTo(UserStatus.ApprovedForSyncing)
    assertThat(loggedInUser.loggedInStatus).isEqualTo(User.LoggedInStatus.LOGGED_IN)
  }
}
