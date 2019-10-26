package org.simple.clinic.main

import com.f2prateek.rx.preferences2.Preference
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.Single
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.activity.ActivityLifecycle.Started
import org.simple.clinic.activity.ActivityLifecycle.Stopped
import org.simple.clinic.forgotpin.createnewpin.ForgotPinCreateNewPinScreenKey
import org.simple.clinic.home.HomeScreenKey
import org.simple.clinic.login.applock.AppLockConfig
import org.simple.clinic.registration.phone.RegistrationPhoneScreenKey
import org.simple.clinic.router.screen.FullScreenKey
import org.simple.clinic.user.NewlyVerifiedUser
import org.simple.clinic.user.User
import org.simple.clinic.user.User.LoggedInStatus.LOGGED_IN
import org.simple.clinic.user.User.LoggedInStatus.NOT_LOGGED_IN
import org.simple.clinic.user.User.LoggedInStatus.OTP_REQUESTED
import org.simple.clinic.user.User.LoggedInStatus.RESETTING_PIN
import org.simple.clinic.user.User.LoggedInStatus.RESET_PIN_REQUESTED
import org.simple.clinic.user.User.LoggedInStatus.UNAUTHORIZED
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.Just
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.Instant
import javax.inject.Inject
import javax.inject.Named

typealias Ui = TheActivity
typealias UiChange = (Ui) -> Unit

class TheActivityController @Inject constructor(
    private val userSession: UserSession,
    private val appLockConfig: Single<AppLockConfig>,
    @Named("should_lock_after") private val lockAfterTimestamp: Preference<Instant>
) : ObservableTransformer<UiEvent, UiChange> {

  private val showAppLockForUserStates = setOf(OTP_REQUESTED, LOGGED_IN, RESET_PIN_REQUESTED)

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = events.compose(ReportAnalyticsEvents()).replay().refCount()

    return Observable.mergeArray(
        showAppLock(replayedEvents),
        updateLockTime(replayedEvents),
        displayUserLoggedOutOnOtherDevice(replayedEvents),
        redirectToLoginScreen()
    )
  }

  private fun showAppLock(events: Observable<UiEvent>): Observable<UiChange> {
    val replayedCanShowAppLock = events
        .ofType<Started>()
        .flatMapMaybe { _ ->
          userSession.loggedInUser()
              .firstElement()
              .filter { it is Just<User> }
              .map { (user) -> user!!.loggedInStatus }
              .defaultIfEmpty(NOT_LOGGED_IN)
        }
        .filter { it in showAppLockForUserStates }
        .map { Instant.now() > lockAfterTimestamp.get() }
        .replay()
        .refCount()

    val showAppLock = replayedCanShowAppLock
        .filter { show -> show }
        .map { { ui: Ui -> ui.showAppLockScreen() } }

    val unsetLockTime = replayedCanShowAppLock
        .filter { show -> !show }
        .flatMap {
          lockAfterTimestamp.delete()
          Observable.empty<UiChange>()
        }

    return unsetLockTime.mergeWith(showAppLock)
  }

  private fun updateLockTime(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<Stopped>()
        .filter { userSession.isUserLoggedIn() }
        .filter { !lockAfterTimestamp.isSet }
        .flatMap { _ ->
          appLockConfig
              .flatMapObservable {
                lockAfterTimestamp.set(Instant.now().plusMillis(it.lockAfterTimeMillis))
                Observable.empty<UiChange>()
              }
        }
  }

  private fun displayUserLoggedOutOnOtherDevice(events: Observable<UiEvent>): Observable<UiChange> {
    return events.ofType<Started>()
        .flatMap { userSession.loggedInUser() }
        .compose(NewlyVerifiedUser())
        .map { { ui: Ui -> ui.showUserLoggedOutOnOtherDeviceAlert() } }
  }

  fun initialScreenKey(): FullScreenKey {
    val localUser = userSession.loggedInUser().blockingFirst().toNullable()

    val canMoveToHomeScreen = when (localUser?.loggedInStatus) {
      NOT_LOGGED_IN, RESETTING_PIN, UNAUTHORIZED -> false
      LOGGED_IN, OTP_REQUESTED, RESET_PIN_REQUESTED -> true
      null -> false
    }

    return when {
      canMoveToHomeScreen -> HomeScreenKey()
      else -> {
        return if (localUser?.loggedInStatus == RESETTING_PIN) {
          ForgotPinCreateNewPinScreenKey()
        } else {
          RegistrationPhoneScreenKey()
        }
      }
    }
  }

  private fun redirectToLoginScreen(): Observable<UiChange> {
    return userSession
        .isUserUnauthorized()
        .distinctUntilChanged()
        .filter { isUserUnauthorized -> isUserUnauthorized }
        .map { Ui::redirectToLogin }
  }
}
