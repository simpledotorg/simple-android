package org.simple.clinic.activity

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.main.ClearLockAfterTimestamp
import org.simple.clinic.main.InitialScreenInfoLoaded
import org.simple.clinic.main.ShowAccessDeniedScreen
import org.simple.clinic.main.ShowForgotPinScreen
import org.simple.clinic.main.ShowHomeScreen
import org.simple.clinic.main.TheActivityModel
import org.simple.clinic.main.TheActivityUpdate
import org.simple.clinic.user.User
import org.simple.clinic.user.UserStatus
import java.time.Instant
import java.util.Optional

class TheActivityUpdateTest {

  private val currentTimestamp = Instant.parse("2018-01-01T00:00:00Z")
  private val lockAtTime = Optional.of(Instant.parse("2018-01-01T00:00:01Z"))
  private val model = TheActivityModel.createForAlreadyLoggedInUser()
  private val spec = UpdateSpec(TheActivityUpdate())

  @Test
  fun `when the local user is waiting for approval and has requested a login OTP, the home screen must be shown`() {
    val user = TestData.loggedInUser(loggedInStatus = User.LoggedInStatus.OTP_REQUESTED, status = UserStatus.WaitingForApproval)

    spec
        .given(model)
        .whenEvent(InitialScreenInfoLoaded(user, currentTimestamp, lockAtTime))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowHomeScreen, ClearLockAfterTimestamp)
        ))
  }

  @Test
  fun `when the local user is waiting for approval and has requested a PIN reset, the home screen must be shown`() {
    val user = TestData.loggedInUser(loggedInStatus = User.LoggedInStatus.RESET_PIN_REQUESTED, status = UserStatus.WaitingForApproval)

    spec
        .given(model)
        .whenEvent(InitialScreenInfoLoaded(user, currentTimestamp, lockAtTime))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowHomeScreen, ClearLockAfterTimestamp)
        ))
  }

  @Test
  fun `when the local user is waiting for approval and has logged in, the home screen must be shown`() {
    val user = TestData.loggedInUser(loggedInStatus = User.LoggedInStatus.LOGGED_IN, status = UserStatus.WaitingForApproval)

    spec
        .given(model)
        .whenEvent(InitialScreenInfoLoaded(user, currentTimestamp, lockAtTime))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowHomeScreen, ClearLockAfterTimestamp)
        ))
  }

  @Test
  fun `when the local user is approved for syncing and has requested a login OTP, the home screen must be shown`() {
    val user = TestData.loggedInUser(loggedInStatus = User.LoggedInStatus.OTP_REQUESTED, status = UserStatus.ApprovedForSyncing)

    spec
        .given(model)
        .whenEvent(InitialScreenInfoLoaded(user, currentTimestamp, lockAtTime))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowHomeScreen, ClearLockAfterTimestamp)
        ))
  }

  @Test
  fun `when the local user is approved for syncing and has requested a PIN reset, the home screen must be shown`() {
    val user = TestData.loggedInUser(loggedInStatus = User.LoggedInStatus.RESET_PIN_REQUESTED, status = UserStatus.ApprovedForSyncing)

    spec
        .given(model)
        .whenEvent(InitialScreenInfoLoaded(user, currentTimestamp, lockAtTime))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowHomeScreen, ClearLockAfterTimestamp)
        ))
  }

  @Test
  fun `when the local user is approved for syncing and has logged in, the home screen must be shown`() {
    val user = TestData.loggedInUser(loggedInStatus = User.LoggedInStatus.LOGGED_IN, status = UserStatus.ApprovedForSyncing)

    spec
        .given(model)
        .whenEvent(InitialScreenInfoLoaded(user, currentTimestamp, lockAtTime))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowHomeScreen, ClearLockAfterTimestamp)
        ))
  }

  @Test
  fun `when the local user is waiting for approval and has been unauthorized, the login screen must be shown`() {
    val user = TestData.loggedInUser(loggedInStatus = User.LoggedInStatus.UNAUTHORIZED, status = UserStatus.WaitingForApproval)

    spec
        .given(model)
        .whenEvent(InitialScreenInfoLoaded(user, currentTimestamp, lockAtTime))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowHomeScreen, ClearLockAfterTimestamp)
        ))
  }

  @Test
  fun `when the local user is approved for syncing and has been unauthorized, the login screen must be shown`() {
    val user = TestData.loggedInUser(loggedInStatus = User.LoggedInStatus.UNAUTHORIZED, status = UserStatus.ApprovedForSyncing)

    spec
        .given(model)
        .whenEvent(InitialScreenInfoLoaded(user, currentTimestamp, lockAtTime))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowHomeScreen, ClearLockAfterTimestamp)
        ))
  }

  @Test
  fun `when the local user is waiting for approval and is resetting the PIN, the create new PIN screen must be shown`() {
    val user = TestData.loggedInUser(loggedInStatus = User.LoggedInStatus.RESETTING_PIN, status = UserStatus.WaitingForApproval)

    spec
        .given(model)
        .whenEvent(InitialScreenInfoLoaded(user, currentTimestamp, lockAtTime))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowForgotPinScreen, ClearLockAfterTimestamp)
        ))
  }

  @Test
  fun `when the local user is approved for syncing and is resetting the PIN, the create new PIN screen must be shown`() {
    val user = TestData.loggedInUser(loggedInStatus = User.LoggedInStatus.RESETTING_PIN, status = UserStatus.ApprovedForSyncing)

    spec
        .given(model)
        .whenEvent(InitialScreenInfoLoaded(user, currentTimestamp, lockAtTime))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowForgotPinScreen, ClearLockAfterTimestamp)
        ))
  }

  @Test
  fun `when the local user is disapproved for syncing and has requested an OTP, the access denied screen must be shown`() {
    val user = TestData.loggedInUser(loggedInStatus = User.LoggedInStatus.OTP_REQUESTED, status = UserStatus.DisapprovedForSyncing)

    spec
        .given(model)
        .whenEvent(InitialScreenInfoLoaded(user, currentTimestamp, lockAtTime))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowAccessDeniedScreen, ClearLockAfterTimestamp)
        ))
  }

  @Test
  fun `when the local user is disapproved for syncing and has requested a PIN reset, the access denied screen must be shown`() {
    val user = TestData.loggedInUser(loggedInStatus = User.LoggedInStatus.RESET_PIN_REQUESTED, status = UserStatus.DisapprovedForSyncing)

    spec
        .given(model)
        .whenEvent(InitialScreenInfoLoaded(user, currentTimestamp, lockAtTime))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowAccessDeniedScreen, ClearLockAfterTimestamp)
        ))
  }

  @Test
  fun `when the local user is disapproved for syncing and is resetting the PIN, the access denied screen must be shown`() {
    val user = TestData.loggedInUser(loggedInStatus = User.LoggedInStatus.RESETTING_PIN, status = UserStatus.DisapprovedForSyncing)

    spec
        .given(model)
        .whenEvent(InitialScreenInfoLoaded(user, currentTimestamp, lockAtTime))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowAccessDeniedScreen, ClearLockAfterTimestamp)
        ))
  }

  @Test
  fun `when the local user is disapproved for syncing and is logged in, the access denied screen must be shown`() {
    val user = TestData.loggedInUser(loggedInStatus = User.LoggedInStatus.LOGGED_IN, status = UserStatus.DisapprovedForSyncing)

    spec
        .given(model)
        .whenEvent(InitialScreenInfoLoaded(user, currentTimestamp, lockAtTime))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowAccessDeniedScreen, ClearLockAfterTimestamp)
        ))
  }

  @Test
  fun `when the local user is disapproved for syncing and is unauthorized, the access denied screen must be shown`() {
    val user = TestData.loggedInUser(loggedInStatus = User.LoggedInStatus.UNAUTHORIZED, status = UserStatus.DisapprovedForSyncing)

    spec
        .given(model)
        .whenEvent(InitialScreenInfoLoaded(user, currentTimestamp, lockAtTime))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowAccessDeniedScreen, ClearLockAfterTimestamp)
        ))
  }
}
