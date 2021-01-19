package org.simple.clinic.main

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.user.User
import org.simple.clinic.user.UserStatus
import org.simple.clinic.util.Optional
import java.time.Instant
import java.util.UUID

class TheActivityUpdateTest {

  private val spec = UpdateSpec(TheActivityUpdate())

  private val freshLoginModel = TheActivityModel.createForNewlyLoggedInUser()

  @Test
  fun `when the local user is waiting for approval and has requested a login OTP, the home screen must be shown`() {
    val user = TestData.loggedInUser(
        uuid = UUID.fromString("d0cd3f2c-1d7d-41af-b09e-d84ca91b6eb2"),
        loggedInStatus = User.LoggedInStatus.OTP_REQUESTED,
        status = UserStatus.WaitingForApproval
    )

    spec
        .given(freshLoginModel)
        .whenEvent(InitialScreenInfoLoaded(
            user = Optional.of(user),
            currentTimestamp = Instant.parse("2018-01-01T00:00:00Z"),
            lockAtTimestamp = Optional.of(Instant.parse("2018-01-01T00:00:01Z"))
        ))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowHomeScreen)
        ))
  }

  @Test
  fun `when the local user is waiting for approval and has requested a PIN reset, the home screen must be shown`() {
    val user = TestData.loggedInUser(
        uuid = UUID.fromString("d0cd3f2c-1d7d-41af-b09e-d84ca91b6eb2"),
        loggedInStatus = User.LoggedInStatus.RESET_PIN_REQUESTED,
        status = UserStatus.WaitingForApproval
    )

    spec
        .given(freshLoginModel)
        .whenEvent(InitialScreenInfoLoaded(
            user = Optional.of(user),
            currentTimestamp = Instant.parse("2018-01-01T00:00:00Z"),
            lockAtTimestamp = Optional.of(Instant.parse("2018-01-01T00:00:01Z"))
        ))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowHomeScreen)
        ))
  }

  @Test
  fun `when the local user is waiting for approval and has logged in, the home screen must be shown`() {
    val user = TestData.loggedInUser(
        uuid = UUID.fromString("d0cd3f2c-1d7d-41af-b09e-d84ca91b6eb2"),
        loggedInStatus = User.LoggedInStatus.LOGGED_IN,
        status = UserStatus.WaitingForApproval
    )

    spec
        .given(freshLoginModel)
        .whenEvent(InitialScreenInfoLoaded(
            user = Optional.of(user),
            currentTimestamp = Instant.parse("2018-01-01T00:00:00Z"),
            lockAtTimestamp = Optional.of(Instant.parse("2018-01-01T00:00:01Z"))
        ))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowHomeScreen)
        ))
  }

  @Test
  fun `when the local user is approved for syncing and has requested a login OTP, the home screen must be shown`() {
    val user = TestData.loggedInUser(
        uuid = UUID.fromString("d0cd3f2c-1d7d-41af-b09e-d84ca91b6eb2"),
        loggedInStatus = User.LoggedInStatus.OTP_REQUESTED,
        status = UserStatus.ApprovedForSyncing
    )

    spec
        .given(freshLoginModel)
        .whenEvent(InitialScreenInfoLoaded(
            user = Optional.of(user),
            currentTimestamp = Instant.parse("2018-01-01T00:00:00Z"),
            lockAtTimestamp = Optional.of(Instant.parse("2018-01-01T00:00:01Z"))
        ))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowHomeScreen)
        ))
  }

  @Test
  fun `when the local user is approved for syncing and has requested a PIN reset, the home screen must be shown`() {
    val user = TestData.loggedInUser(
        uuid = UUID.fromString("d0cd3f2c-1d7d-41af-b09e-d84ca91b6eb2"),
        loggedInStatus = User.LoggedInStatus.RESET_PIN_REQUESTED,
        status = UserStatus.ApprovedForSyncing
    )

    spec
        .given(freshLoginModel)
        .whenEvent(InitialScreenInfoLoaded(
            user = Optional.of(user),
            currentTimestamp = Instant.parse("2018-01-01T00:00:00Z"),
            lockAtTimestamp = Optional.of(Instant.parse("2018-01-01T00:00:01Z"))
        ))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowHomeScreen)
        ))
  }

  @Test
  fun `when the local user is approved for syncing and has logged in, the home screen must be shown`() {
    val user = TestData.loggedInUser(
        uuid = UUID.fromString("d0cd3f2c-1d7d-41af-b09e-d84ca91b6eb2"),
        loggedInStatus = User.LoggedInStatus.LOGGED_IN,
        status = UserStatus.ApprovedForSyncing
    )

    spec
        .given(freshLoginModel)
        .whenEvent(InitialScreenInfoLoaded(
            user = Optional.of(user),
            currentTimestamp = Instant.parse("2018-01-01T00:00:00Z"),
            lockAtTimestamp = Optional.of(Instant.parse("2018-01-01T00:00:01Z"))
        ))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowHomeScreen)
        ))
  }

  @Test
  fun `when the local user is waiting for approval and has been unauthorized, the login screen must be shown`() {
    val user = TestData.loggedInUser(
        uuid = UUID.fromString("d0cd3f2c-1d7d-41af-b09e-d84ca91b6eb2"),
        loggedInStatus = User.LoggedInStatus.UNAUTHORIZED,
        status = UserStatus.WaitingForApproval
    )

    spec
        .given(freshLoginModel)
        .whenEvent(InitialScreenInfoLoaded(
            user = Optional.of(user),
            currentTimestamp = Instant.parse("2018-01-01T00:00:00Z"),
            lockAtTimestamp = Optional.of(Instant.parse("2018-01-01T00:00:01Z"))
        ))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowHomeScreen)
        ))
  }

  @Test
  fun `when the local user is approved for syncing and has been unauthorized, the login screen must be shown`() {
    val user = TestData.loggedInUser(
        uuid = UUID.fromString("d0cd3f2c-1d7d-41af-b09e-d84ca91b6eb2"),
        loggedInStatus = User.LoggedInStatus.UNAUTHORIZED,
        status = UserStatus.ApprovedForSyncing
    )

    spec
        .given(freshLoginModel)
        .whenEvent(InitialScreenInfoLoaded(
            user = Optional.of(user),
            currentTimestamp = Instant.parse("2018-01-01T00:00:00Z"),
            lockAtTimestamp = Optional.of(Instant.parse("2018-01-01T00:00:01Z"))
        ))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowHomeScreen)
        ))
  }

  @Test
  fun `when the local user is waiting for approval and is resetting the PIN, the create new PIN screen must be shown`() {
    val user = TestData.loggedInUser(
        uuid = UUID.fromString("d0cd3f2c-1d7d-41af-b09e-d84ca91b6eb2"),
        loggedInStatus = User.LoggedInStatus.RESETTING_PIN,
        status = UserStatus.WaitingForApproval
    )

    spec
        .given(freshLoginModel)
        .whenEvent(InitialScreenInfoLoaded(
            user = Optional.of(user),
            currentTimestamp = Instant.parse("2018-01-01T00:00:00Z"),
            lockAtTimestamp = Optional.of(Instant.parse("2018-01-01T00:00:01Z"))
        ))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowForgotPinCreatePinScreen)
        ))
  }

  @Test
  fun `when the local user is approved for syncing and is resetting the PIN, the create new PIN screen must be shown`() {
    val user = TestData.loggedInUser(
        uuid = UUID.fromString("d0cd3f2c-1d7d-41af-b09e-d84ca91b6eb2"),
        loggedInStatus = User.LoggedInStatus.RESETTING_PIN,
        status = UserStatus.ApprovedForSyncing
    )

    spec
        .given(freshLoginModel)
        .whenEvent(InitialScreenInfoLoaded(
            user = Optional.of(user),
            currentTimestamp = Instant.parse("2018-01-01T00:00:00Z"),
            lockAtTimestamp = Optional.of(Instant.parse("2018-01-01T00:00:01Z"))
        ))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowForgotPinCreatePinScreen)
        ))
  }

  @Test
  fun `when the local user is disapproved for syncing and has requested an OTP, the access denied screen must be shown`() {
    val user = TestData.loggedInUser(
        uuid = UUID.fromString("d0cd3f2c-1d7d-41af-b09e-d84ca91b6eb2"),
        loggedInStatus = User.LoggedInStatus.OTP_REQUESTED,
        status = UserStatus.DisapprovedForSyncing
    )

    spec
        .given(freshLoginModel)
        .whenEvent(InitialScreenInfoLoaded(
            user = Optional.of(user),
            currentTimestamp = Instant.parse("2018-01-01T00:00:00Z"),
            lockAtTimestamp = Optional.of(Instant.parse("2018-01-01T00:00:01Z"))
        ))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowAccessDeniedScreen)
        ))
  }

  @Test
  fun `when the local user is disapproved for syncing and has requested a PIN reset, the access denied screen must be shown`() {
    val user = TestData.loggedInUser(
        uuid = UUID.fromString("d0cd3f2c-1d7d-41af-b09e-d84ca91b6eb2"),
        loggedInStatus = User.LoggedInStatus.RESET_PIN_REQUESTED,
        status = UserStatus.DisapprovedForSyncing
    )

    spec
        .given(freshLoginModel)
        .whenEvent(InitialScreenInfoLoaded(
            user = Optional.of(user),
            currentTimestamp = Instant.parse("2018-01-01T00:00:00Z"),
            lockAtTimestamp = Optional.of(Instant.parse("2018-01-01T00:00:01Z"))
        ))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowAccessDeniedScreen)
        ))
  }

  @Test
  fun `when the local user is disapproved for syncing and is resetting the PIN, the access denied screen must be shown`() {
    val user = TestData.loggedInUser(
        uuid = UUID.fromString("d0cd3f2c-1d7d-41af-b09e-d84ca91b6eb2"),
        loggedInStatus = User.LoggedInStatus.RESETTING_PIN,
        status = UserStatus.DisapprovedForSyncing
    )

    spec
        .given(freshLoginModel)
        .whenEvent(InitialScreenInfoLoaded(
            user = Optional.of(user),
            currentTimestamp = Instant.parse("2018-01-01T00:00:00Z"),
            lockAtTimestamp = Optional.of(Instant.parse("2018-01-01T00:00:01Z"))
        ))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowAccessDeniedScreen)
        ))
  }

  @Test
  fun `when the local user is disapproved for syncing and is logged in, the access denied screen must be shown`() {
    val user = TestData.loggedInUser(
        uuid = UUID.fromString("d0cd3f2c-1d7d-41af-b09e-d84ca91b6eb2"),
        loggedInStatus = User.LoggedInStatus.LOGGED_IN,
        status = UserStatus.DisapprovedForSyncing
    )

    spec
        .given(freshLoginModel)
        .whenEvent(InitialScreenInfoLoaded(
            user = Optional.of(user),
            currentTimestamp = Instant.parse("2018-01-01T00:00:00Z"),
            lockAtTimestamp = Optional.of(Instant.parse("2018-01-01T00:00:01Z"))
        ))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowAccessDeniedScreen)
        ))
  }

  @Test
  fun `when the local user is disapproved for syncing and is unauthorized, the access denied screen must be shown`() {
    val user = TestData.loggedInUser(
        uuid = UUID.fromString("d0cd3f2c-1d7d-41af-b09e-d84ca91b6eb2"),
        loggedInStatus = User.LoggedInStatus.UNAUTHORIZED,
        status = UserStatus.DisapprovedForSyncing
    )

    spec
        .given(freshLoginModel)
        .whenEvent(InitialScreenInfoLoaded(
            user = Optional.of(user),
            currentTimestamp = Instant.parse("2018-01-01T00:00:00Z"),
            lockAtTimestamp = Optional.of(Instant.parse("2018-01-01T00:00:01Z"))
        ))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowAccessDeniedScreen)
        ))
  }
}
