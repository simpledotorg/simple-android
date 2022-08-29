package org.simple.clinic.activity

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.sharedTestCode.TestData
import org.simple.clinic.deniedaccess.AccessDeniedScreenKey
import org.simple.clinic.empty.EmptyScreenKey
import org.simple.clinic.forgotpin.createnewpin.ForgotPinCreateNewPinScreen
import org.simple.clinic.home.HomeScreenKey
import org.simple.clinic.login.applock.AppLockScreenKey
import org.simple.clinic.main.ClearLockAfterTimestamp
import org.simple.clinic.main.InitialScreenInfoLoaded
import org.simple.clinic.main.SetCurrentScreenHistory
import org.simple.clinic.main.TheActivityModel
import org.simple.clinic.main.TheActivityUpdate
import org.simple.clinic.navigation.v2.History
import org.simple.clinic.navigation.v2.compat.wrap
import org.simple.clinic.scanid.OpenedFrom
import org.simple.clinic.scanid.ScanSimpleIdScreenKey
import org.simple.clinic.user.User
import org.simple.clinic.user.UserStatus
import java.time.Instant
import java.util.Optional
import java.util.UUID

class TheActivityUpdateTest {

  private val currentTimestamp = Instant.parse("2018-01-01T00:00:00Z")
  private val lockAtTime = Optional.of(Instant.parse("2018-01-01T00:00:01Z"))
  private val model = TheActivityModel.createForAlreadyLoggedInUser()
  private val spec = UpdateSpec(TheActivityUpdate())
  private val initialHistoryScreen = History.ofNormalScreens(EmptyScreenKey().wrap())

  @Test
  fun `when the local user is waiting for approval and has requested a login OTP, the home screen must be shown`() {
    val user = TestData.loggedInUser(loggedInStatus = User.LoggedInStatus.OTP_REQUESTED, status = UserStatus.WaitingForApproval)

    spec
        .given(model)
        .whenEvent(InitialScreenInfoLoaded(user, currentTimestamp, lockAtTime, initialHistoryScreen))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(SetCurrentScreenHistory(History.ofNormalScreens(HomeScreenKey)), ClearLockAfterTimestamp)
        ))
  }

  @Test
  fun `when the local user is waiting for approval and has requested a PIN reset, the home screen must be shown`() {
    val user = TestData.loggedInUser(loggedInStatus = User.LoggedInStatus.RESET_PIN_REQUESTED, status = UserStatus.WaitingForApproval)

    spec
        .given(model)
        .whenEvent(InitialScreenInfoLoaded(user, currentTimestamp, lockAtTime, initialHistoryScreen))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(SetCurrentScreenHistory(History.ofNormalScreens(HomeScreenKey)), ClearLockAfterTimestamp)
        ))
  }

  @Test
  fun `when the local user is waiting for approval and has logged in, the home screen must be shown`() {
    val user = TestData.loggedInUser(loggedInStatus = User.LoggedInStatus.LOGGED_IN, status = UserStatus.WaitingForApproval)

    spec
        .given(model)
        .whenEvent(InitialScreenInfoLoaded(user, currentTimestamp, lockAtTime, initialHistoryScreen))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(SetCurrentScreenHistory(History.ofNormalScreens(HomeScreenKey)), ClearLockAfterTimestamp)
        ))
  }

  @Test
  fun `when the local user is approved for syncing and has requested a login OTP, the home screen must be shown`() {
    val user = TestData.loggedInUser(loggedInStatus = User.LoggedInStatus.OTP_REQUESTED, status = UserStatus.ApprovedForSyncing)

    spec
        .given(model)
        .whenEvent(InitialScreenInfoLoaded(user, currentTimestamp, lockAtTime, initialHistoryScreen))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(SetCurrentScreenHistory(History.ofNormalScreens(HomeScreenKey)), ClearLockAfterTimestamp)
        ))
  }

  @Test
  fun `when the local user is approved for syncing and has requested a PIN reset, the home screen must be shown`() {
    val user = TestData.loggedInUser(loggedInStatus = User.LoggedInStatus.RESET_PIN_REQUESTED, status = UserStatus.ApprovedForSyncing)

    spec
        .given(model)
        .whenEvent(InitialScreenInfoLoaded(user, currentTimestamp, lockAtTime, initialHistoryScreen))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(SetCurrentScreenHistory(History.ofNormalScreens(HomeScreenKey)), ClearLockAfterTimestamp)
        ))
  }

  @Test
  fun `when the local user is approved for syncing and has logged in, the home screen must be shown`() {
    val user = TestData.loggedInUser(loggedInStatus = User.LoggedInStatus.LOGGED_IN, status = UserStatus.ApprovedForSyncing)

    spec
        .given(model)
        .whenEvent(InitialScreenInfoLoaded(user, currentTimestamp, lockAtTime, initialHistoryScreen))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(SetCurrentScreenHistory(History.ofNormalScreens(HomeScreenKey)), ClearLockAfterTimestamp)
        ))
  }

  @Test
  fun `when the local user is waiting for approval and has been unauthorized, the login screen must be shown`() {
    val user = TestData.loggedInUser(loggedInStatus = User.LoggedInStatus.UNAUTHORIZED, status = UserStatus.WaitingForApproval)

    spec
        .given(model)
        .whenEvent(InitialScreenInfoLoaded(user, currentTimestamp, lockAtTime, initialHistoryScreen))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(SetCurrentScreenHistory(History.ofNormalScreens(HomeScreenKey)), ClearLockAfterTimestamp)
        ))
  }

  @Test
  fun `when the local user is approved for syncing and has been unauthorized, the login screen must be shown`() {
    val user = TestData.loggedInUser(loggedInStatus = User.LoggedInStatus.UNAUTHORIZED, status = UserStatus.ApprovedForSyncing)

    spec
        .given(model)
        .whenEvent(InitialScreenInfoLoaded(user, currentTimestamp, lockAtTime, initialHistoryScreen))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(SetCurrentScreenHistory(History.ofNormalScreens(HomeScreenKey)), ClearLockAfterTimestamp)
        ))
  }

  @Test
  fun `when the local user is waiting for approval and is resetting the PIN, the create new PIN screen must be shown`() {
    val user = TestData.loggedInUser(loggedInStatus = User.LoggedInStatus.RESETTING_PIN, status = UserStatus.WaitingForApproval)

    spec
        .given(model)
        .whenEvent(InitialScreenInfoLoaded(user, currentTimestamp, lockAtTime, initialHistoryScreen))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(SetCurrentScreenHistory(History.ofNormalScreens(ForgotPinCreateNewPinScreen.Key())), ClearLockAfterTimestamp)
        ))
  }

  @Test
  fun `when the local user is approved for syncing and is resetting the PIN, the create new PIN screen must be shown`() {
    val user = TestData.loggedInUser(loggedInStatus = User.LoggedInStatus.RESETTING_PIN, status = UserStatus.ApprovedForSyncing)

    spec
        .given(model)
        .whenEvent(InitialScreenInfoLoaded(user, currentTimestamp, lockAtTime, initialHistoryScreen))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(SetCurrentScreenHistory(History.ofNormalScreens(ForgotPinCreateNewPinScreen.Key())), ClearLockAfterTimestamp)
        ))
  }

  @Test
  fun `when the local user is disapproved for syncing and has requested an OTP, the access denied screen must be shown`() {
    val user = TestData.loggedInUser(loggedInStatus = User.LoggedInStatus.OTP_REQUESTED, status = UserStatus.DisapprovedForSyncing)

    spec
        .given(model)
        .whenEvent(InitialScreenInfoLoaded(user, currentTimestamp, lockAtTime, initialHistoryScreen))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(SetCurrentScreenHistory(History.ofNormalScreens(AccessDeniedScreenKey(user.fullName))), ClearLockAfterTimestamp)
        ))
  }

  @Test
  fun `when the local user is disapproved for syncing and has requested a PIN reset, the access denied screen must be shown`() {
    val user = TestData.loggedInUser(loggedInStatus = User.LoggedInStatus.RESET_PIN_REQUESTED, status = UserStatus.DisapprovedForSyncing)

    spec
        .given(model)
        .whenEvent(InitialScreenInfoLoaded(user, currentTimestamp, lockAtTime, initialHistoryScreen))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(SetCurrentScreenHistory(History.ofNormalScreens(AccessDeniedScreenKey(user.fullName))), ClearLockAfterTimestamp)
        ))
  }

  @Test
  fun `when the local user is disapproved for syncing and is resetting the PIN, the access denied screen must be shown`() {
    val user = TestData.loggedInUser(loggedInStatus = User.LoggedInStatus.RESETTING_PIN, status = UserStatus.DisapprovedForSyncing)

    spec
        .given(model)
        .whenEvent(InitialScreenInfoLoaded(user, currentTimestamp, lockAtTime, initialHistoryScreen))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(SetCurrentScreenHistory(History.ofNormalScreens(AccessDeniedScreenKey(user.fullName))), ClearLockAfterTimestamp)
        ))
  }

  @Test
  fun `when the local user is disapproved for syncing and is logged in, the access denied screen must be shown`() {
    val user = TestData.loggedInUser(loggedInStatus = User.LoggedInStatus.LOGGED_IN, status = UserStatus.DisapprovedForSyncing)

    spec
        .given(model)
        .whenEvent(InitialScreenInfoLoaded(user, currentTimestamp, lockAtTime, initialHistoryScreen))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(SetCurrentScreenHistory(History.ofNormalScreens(AccessDeniedScreenKey(user.fullName))), ClearLockAfterTimestamp)
        ))
  }

  @Test
  fun `when the local user is disapproved for syncing and is unauthorized, the access denied screen must be shown`() {
    val user = TestData.loggedInUser(loggedInStatus = User.LoggedInStatus.UNAUTHORIZED, status = UserStatus.DisapprovedForSyncing)

    spec
        .given(model)
        .whenEvent(InitialScreenInfoLoaded(user, currentTimestamp, lockAtTime, initialHistoryScreen))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(SetCurrentScreenHistory(History.ofNormalScreens(AccessDeniedScreenKey(user.fullName))), ClearLockAfterTimestamp)
        ))
  }

  @Test
  fun `when the user is already logged in and the screen is opened, show the lock screen`() {
    val user = TestData.loggedInUser(
        uuid = UUID.fromString("233204e2-d64f-4fd6-ab64-a99d9289609f"),
        loggedInStatus = User.LoggedInStatus.LOGGED_IN,
        status = UserStatus.ApprovedForSyncing
    )

    val model = TheActivityModel.createForAlreadyLoggedInUser()

    spec
        .given(model)
        .whenEvent(InitialScreenInfoLoaded(
            user = user,
            currentTimestamp = Instant.parse("2018-01-01T00:00:00Z"),
            lockAtTimestamp = Optional.empty(),
            currentHistory = initialHistoryScreen
        ))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(SetCurrentScreenHistory(History.ofNormalScreens(AppLockScreenKey(History.ofNormalScreens(HomeScreenKey)))))
        ))
  }

  @Test
  fun `when the user has just logged in and the screen is opened, don't show the lock screen`() {
    val user = TestData.loggedInUser(
        uuid = UUID.fromString("233204e2-d64f-4fd6-ab64-a99d9289609f"),
        loggedInStatus = User.LoggedInStatus.LOGGED_IN,
        status = UserStatus.ApprovedForSyncing
    )

    val model = TheActivityModel.createForNewlyLoggedInUser()

    spec
        .given(model)
        .whenEvent(InitialScreenInfoLoaded(
            user = user,
            currentTimestamp = Instant.parse("2018-01-01T00:00:00Z"),
            lockAtTimestamp = Optional.empty(),
            currentHistory = initialHistoryScreen
        ))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(SetCurrentScreenHistory(History.ofNormalScreens(HomeScreenKey)))
        ))
  }

  @Test
  fun `when the local user is approved for syncing and has logged in, then restore the history if it's not empty`() {
    val user = TestData.loggedInUser(loggedInStatus = User.LoggedInStatus.LOGGED_IN, status = UserStatus.ApprovedForSyncing)
    val currentHistory = History.ofNormalScreens(HomeScreenKey, ScanSimpleIdScreenKey(OpenedFrom.PatientsTabScreen))
    spec
        .given(model)
        .whenEvent(InitialScreenInfoLoaded(user, currentTimestamp, lockAtTime, currentHistory))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(SetCurrentScreenHistory(currentHistory), ClearLockAfterTimestamp)
        ))
  }

  @Test
  fun `when the user is already logged in and the screen is opened, show the lock screen with the current history`() {
    val user = TestData.loggedInUser(
        uuid = UUID.fromString("233204e2-d64f-4fd6-ab64-a99d9289609f"),
        loggedInStatus = User.LoggedInStatus.LOGGED_IN,
        status = UserStatus.ApprovedForSyncing
    )

    val model = TheActivityModel.createForAlreadyLoggedInUser()
    val currentHistory = History.ofNormalScreens(HomeScreenKey, ScanSimpleIdScreenKey(OpenedFrom.PatientsTabScreen))

    spec
        .given(model)
        .whenEvent(InitialScreenInfoLoaded(
            user = user,
            currentTimestamp = Instant.parse("2018-01-01T00:00:00Z"),
            lockAtTimestamp = Optional.empty(),
            currentHistory = currentHistory
        ))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(SetCurrentScreenHistory(History.ofNormalScreens(AppLockScreenKey(currentHistory))))
        ))
  }
}
