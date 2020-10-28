package org.simple.clinic.activity

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.deniedaccess.AccessDeniedScreenKey
import org.simple.clinic.forgotpin.createnewpin.ForgotPinCreateNewPinScreenKey
import org.simple.clinic.home.HomeScreenKey
import org.simple.clinic.main.initialScreenKey
import org.simple.clinic.user.User
import org.simple.clinic.user.UserStatus

class TheActivityInitialScreenKeyTest {

  @Test
  fun `when the local user is waiting for approval and has requested a login OTP, the home screen must be shown`() {
    val user = TestData.loggedInUser(loggedInStatus = User.LoggedInStatus.OTP_REQUESTED, status = UserStatus.WaitingForApproval)

    assertThat(initialScreenKey(user)).isInstanceOf(HomeScreenKey::class.java)
  }

  @Test
  fun `when the local user is waiting for approval and has requested a PIN reset, the home screen must be shown`() {
    val user = TestData.loggedInUser(loggedInStatus = User.LoggedInStatus.RESET_PIN_REQUESTED, status = UserStatus.WaitingForApproval)

    assertThat(initialScreenKey(user)).isInstanceOf(HomeScreenKey::class.java)
  }

  @Test
  fun `when the local user is waiting for approval and has logged in, the home screen must be shown`() {
    val user = TestData.loggedInUser(loggedInStatus = User.LoggedInStatus.LOGGED_IN, status = UserStatus.WaitingForApproval)

    assertThat(initialScreenKey(user)).isInstanceOf(HomeScreenKey::class.java)
  }

  @Test
  fun `when the local user is approved for syncing and has requested a login OTP, the home screen must be shown`() {
    val user = TestData.loggedInUser(loggedInStatus = User.LoggedInStatus.OTP_REQUESTED, status = UserStatus.ApprovedForSyncing)

    assertThat(initialScreenKey(user)).isInstanceOf(HomeScreenKey::class.java)
  }

  @Test
  fun `when the local user is approved for syncing and has requested a PIN reset, the home screen must be shown`() {
    val user = TestData.loggedInUser(loggedInStatus = User.LoggedInStatus.RESET_PIN_REQUESTED, status = UserStatus.ApprovedForSyncing)

    assertThat(initialScreenKey(user)).isInstanceOf(HomeScreenKey::class.java)
  }

  @Test
  fun `when the local user is approved for syncing and has logged in, the home screen must be shown`() {
    val user = TestData.loggedInUser(loggedInStatus = User.LoggedInStatus.LOGGED_IN, status = UserStatus.ApprovedForSyncing)

    assertThat(initialScreenKey(user)).isInstanceOf(HomeScreenKey::class.java)
  }

  @Test
  fun `when the local user is waiting for approval and has been unauthorized, the login screen must be shown`() {
    val user = TestData.loggedInUser(loggedInStatus = User.LoggedInStatus.UNAUTHORIZED, status = UserStatus.WaitingForApproval)

    assertThat(initialScreenKey(user)).isInstanceOf(HomeScreenKey::class.java)
  }

  @Test
  fun `when the local user is approved for syncing and has been unauthorized, the login screen must be shown`() {
    val user = TestData.loggedInUser(loggedInStatus = User.LoggedInStatus.UNAUTHORIZED, status = UserStatus.ApprovedForSyncing)

    assertThat(initialScreenKey(user)).isInstanceOf(HomeScreenKey::class.java)
  }

  @Test
  fun `when the local user is waiting for approval and is resetting the PIN, the create new PIN screen must be shown`() {
    val user = TestData.loggedInUser(loggedInStatus = User.LoggedInStatus.RESETTING_PIN, status = UserStatus.WaitingForApproval)

    assertThat(initialScreenKey(user)).isInstanceOf(ForgotPinCreateNewPinScreenKey::class.java)
  }

  @Test
  fun `when the local user is approved for syncing and is resetting the PIN, the create new PIN screen must be shown`() {
    val user = TestData.loggedInUser(loggedInStatus = User.LoggedInStatus.RESETTING_PIN, status = UserStatus.ApprovedForSyncing)

    assertThat(initialScreenKey(user)).isInstanceOf(ForgotPinCreateNewPinScreenKey::class.java)
  }

  @Test
  fun `when the local user is disapproved for syncing and is not logged in, the access denied screen must be shown`() {
    val user = TestData.loggedInUser(loggedInStatus = User.LoggedInStatus.NOT_LOGGED_IN, status = UserStatus.DisapprovedForSyncing)

    assertThat(initialScreenKey(user)).isInstanceOf(AccessDeniedScreenKey::class.java)
  }

  @Test
  fun `when the local user is disapproved for syncing and has requested an OTP, the access denied screen must be shown`() {
    val user = TestData.loggedInUser(loggedInStatus = User.LoggedInStatus.OTP_REQUESTED, status = UserStatus.DisapprovedForSyncing)

    assertThat(initialScreenKey(user)).isInstanceOf(AccessDeniedScreenKey::class.java)
  }

  @Test
  fun `when the local user is disapproved for syncing and has requested a PIN reset, the access denied screen must be shown`() {
    val user = TestData.loggedInUser(loggedInStatus = User.LoggedInStatus.RESET_PIN_REQUESTED, status = UserStatus.DisapprovedForSyncing)

    assertThat(initialScreenKey(user)).isInstanceOf(AccessDeniedScreenKey::class.java)
  }

  @Test
  fun `when the local user is disapproved for syncing and is resetting the PIN, the access denied screen must be shown`() {
    val user = TestData.loggedInUser(loggedInStatus = User.LoggedInStatus.RESETTING_PIN, status = UserStatus.DisapprovedForSyncing)

    assertThat(initialScreenKey(user)).isInstanceOf(AccessDeniedScreenKey::class.java)
  }

  @Test
  fun `when the local user is disapproved for syncing and is logged in, the access denied screen must be shown`() {
    val user = TestData.loggedInUser(loggedInStatus = User.LoggedInStatus.LOGGED_IN, status = UserStatus.DisapprovedForSyncing)

    assertThat(initialScreenKey(user)).isInstanceOf(AccessDeniedScreenKey::class.java)
  }

  @Test
  fun `when the local user is disapproved for syncing and is unauthorized, the access denied screen must be shown`() {
    val user = TestData.loggedInUser(loggedInStatus = User.LoggedInStatus.UNAUTHORIZED, status = UserStatus.DisapprovedForSyncing)

    assertThat(initialScreenKey(user)).isInstanceOf(AccessDeniedScreenKey::class.java)
  }
}
