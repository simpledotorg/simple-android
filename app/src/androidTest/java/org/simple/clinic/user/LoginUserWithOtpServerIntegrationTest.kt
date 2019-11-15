package org.simple.clinic.user

import androidx.test.runner.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.TestClinicApp
import org.simple.clinic.TestData
import org.simple.clinic.login.LoginResult
import org.simple.clinic.rules.ServerAuthenticationRule
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
class LoginUserWithOtpServerIntegrationTest {

  @get:Rule
  val authenticationRule = ServerAuthenticationRule()

  @Inject
  lateinit var userSession: UserSession

  @Inject
  lateinit var loginUserWithOtp: LoginUserWithOtp

  @Inject
  lateinit var testData: TestData

  @Before
  fun setUp() {
    TestClinicApp.appComponent().inject(this)
  }

  @Test
  fun when_correct_login_params_are_given_then_login_should_happen_and_session_data_should_be_persisted() {
    val user = userSession.loggedInUserImmediate()!!

    val loginResult = loginUserWithOtp.loginWithOtp(user.phoneNumber, testData.qaUserPin(), testData.qaUserOtp())
        .blockingGet()

    assertThat(loginResult).isInstanceOf(LoginResult.Success::class.java)
    val (accessToken) = userSession.accessToken()
    assertThat(accessToken).isNotNull()

    val (loggedInUser) = userSession.loggedInUser().blockingFirst()
    assertThat(userSession.isUserLoggedIn()).isTrue()
    assertThat(loggedInUser!!.status).isEqualTo(UserStatus.ApprovedForSyncing)
    assertThat(loggedInUser.loggedInStatus).isEqualTo(User.LoggedInStatus.LOGGED_IN)
  }
}
