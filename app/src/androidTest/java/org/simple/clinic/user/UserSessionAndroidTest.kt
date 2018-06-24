package org.simple.clinic.user

import android.support.test.runner.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.TestClinicApp
import org.simple.clinic.login.LoginResult
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
  fun whencorrectloginparams_loginshouldhappen() {
    val lawgon = userSession
        .saveOngoingLoginEntry(OngoingLoginEntry(otp = "0000", phoneNumber = "0000", pin = "0000"))
        .andThen(userSession.login())
        .blockingGet()

    assertThat(lawgon is LoginResult.Success)
  }

  @Test
  fun whenwrongloginparams_loginshouldfailnicely() {
    val lawgon = userSession
        .saveOngoingLoginEntry(OngoingLoginEntry(otp = "123", phoneNumber = "9999", pin = "0000"))
        .andThen(userSession.login())
        .blockingGet()

    assertThat(lawgon is LoginResult.UnexpectedError)
  }
}
