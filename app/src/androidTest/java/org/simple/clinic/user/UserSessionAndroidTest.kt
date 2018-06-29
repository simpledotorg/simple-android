package org.simple.clinic.user

import android.support.test.runner.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.TestClinicApp
import org.simple.clinic.login.LoginResult
import org.simple.clinic.util.Just
import java.util.UUID
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
class UserSessionAndroidTest {

  @Inject
  lateinit var userSession: UserSession

  @Before
  fun setUp() {
    TestClinicApp.appComponent().inject(this)
  }

  @Test
  fun whenCorrectLoginParametersAreGiven_LoginShouldHappen_AndSessionDataShouldBePersisted() {
    val lawgon = userSession
        .saveOngoingLoginEntry(TestClinicApp.qaOngoingLoginEntry())
        .andThen(userSession.login())
        .blockingGet()

    assertThat(lawgon).isInstanceOf(LoginResult.Success::class.java)
    assertThat(userSession.accessToken()).isEqualTo(Just("7d728cc7e54aa148e84befda6d6d570f67ac60b3410445a1fb0e8d2216fcde44"))

    val (loggedInUser) = userSession.loggedInUser().blockingFirst()
    assertThat(userSession.isUserLoggedIn()).isTrue()
    assertThat(loggedInUser!!.facilityUuid).isEqualTo(UUID.fromString("43dad34c-139e-4e5f-976e-a3ef1d9ac977"))
  }

  @Test
  fun whenIncorrectLoginParameteresAreGiven_LoginShouldFail() {
    val lawgon = userSession
        .saveOngoingLoginEntry(OngoingLoginEntry("8721", "9919299", "0102"))
        .andThen(userSession.login())
        .blockingGet()

    assertThat(lawgon).isInstanceOf(LoginResult.ServerError::class.java)
    assertThat(userSession.isUserLoggedIn()).isFalse()

    val (accessToken) = userSession.accessToken()
    assertThat(accessToken).isNull()
  }

  @After
  fun tearDown() {
    userSession.logout()
  }
}
