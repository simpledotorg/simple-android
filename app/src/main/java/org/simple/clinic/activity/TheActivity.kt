package org.simple.clinic.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.WindowManager
import com.f2prateek.rx.preferences2.Preference
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.BuildConfig
import org.simple.clinic.ClinicApp
import org.simple.clinic.R
import org.simple.clinic.analytics.Analytics
import org.simple.clinic.home.HomeScreen
import org.simple.clinic.home.patients.LoggedOutOnOtherDeviceDialog
import org.simple.clinic.login.applock.AppLockScreen
import org.simple.clinic.onboarding.OnboardingScreen
import org.simple.clinic.registration.phone.RegistrationPhoneScreen
import org.simple.clinic.router.ScreenResultBus
import org.simple.clinic.router.screen.ActivityPermissionResult
import org.simple.clinic.router.screen.ActivityResult
import org.simple.clinic.router.screen.FullScreenKey
import org.simple.clinic.router.screen.FullScreenKeyChanger
import org.simple.clinic.router.screen.NestedKeyChanger
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.user.User
import org.simple.clinic.user.User.*
import org.simple.clinic.user.User.LoggedInStatus.*
import org.simple.clinic.user.UserSession
import org.simple.clinic.widgets.TheActivityLifecycle
import javax.inject.Inject
import javax.inject.Named

class TheActivity : AppCompatActivity() {

  companion object {
    lateinit var component: TheActivityComponent
  }

  @Inject
  lateinit var userSession: UserSession

  @Inject
  lateinit var controller: TheActivityController

  @Inject
  lateinit var lifecycle: Observable<TheActivityLifecycle>

  @Inject
  @field:Named("onboarding_complete")
  lateinit var hasUserCompletedOnboarding: Preference<Boolean>

  lateinit var screenRouter: ScreenRouter

  private val screenResults: ScreenResultBus = ScreenResultBus()

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)
    @Suppress("ConstantConditionIf")
    if (BuildConfig.DISABLE_SCREENSHOT) {
      window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
    }

    lifecycle
        .startWith(TheActivityLifecycle.Started())
        .compose(controller)
        .takeUntil(lifecycle.ofType<TheActivityLifecycle.Destroyed>())
        .subscribe { uiChange -> uiChange(this) }
  }

  override fun attachBaseContext(baseContext: Context) {
    val contextWithRouter = wrapContextWithRouter(baseContext)
    super.attachBaseContext(ViewPumpContextWrapper.wrap(contextWithRouter))
  }

  private fun wrapContextWithRouter(baseContext: Context): Context {
    screenRouter = ScreenRouter.create(this, NestedKeyChanger(), screenResults)
    component = ClinicApp.appComponent
        .activityComponentBuilder()
        .activity(this)
        .screenRouter(screenRouter)
        .build()
    component.inject(this)

    screenRouter.registerKeyChanger(FullScreenKeyChanger(this, android.R.id.content, R.color.window_background, this::onScreenChanged))
    return screenRouter.installInContext(baseContext, initialScreenKey())
  }

  private fun onScreenChanged(outgoing: FullScreenKey?, incoming: FullScreenKey) {
    val outgoingScreenName = outgoing?.analyticsName ?: ""
    val incomingScreenName = incoming.analyticsName
    Analytics.reportScreenChange(outgoingScreenName, incomingScreenName)
  }

  private fun initialScreenKey(): FullScreenKey {
    val localUser = userSession.loggedInUser().blockingFirst().toNullable()

    // TODO: Figure out how to handle the new PIN reset status
    val canMoveToHomeScreen = when (localUser?.loggedInStatus) {
      NOT_LOGGED_IN, RESETTING_PIN -> false
      LOGGED_IN, OTP_REQUESTED, RESET_PIN_REQUESTED  -> true
      null -> false
    }

    return when {
      canMoveToHomeScreen -> HomeScreen.KEY
      hasUserCompletedOnboarding.get().not() -> OnboardingScreen.KEY
      else -> RegistrationPhoneScreen.KEY
    }
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    screenResults.send(ActivityResult(requestCode, resultCode, data))
  }

  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    screenResults.send(ActivityPermissionResult(requestCode))
  }

  override fun onBackPressed() {
    val interceptCallback = screenRouter.offerBackPressToInterceptors()
    if (interceptCallback.intercepted) {
      return
    }
    val popCallback = screenRouter.pop()
    if (popCallback.popped) {
      return
    }
    super.onBackPressed()
  }

  fun showAppLockScreen() {
    screenRouter.push(AppLockScreen.KEY)
  }

  // This is here because we need to show the same alert in multiple
  // screens when the user gets verified in the background.
  fun showUserLoggedOutOnOtherDeviceAlert() {
    LoggedOutOnOtherDeviceDialog.show(supportFragmentManager)
  }
}
