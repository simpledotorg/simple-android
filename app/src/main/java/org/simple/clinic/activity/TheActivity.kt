package org.simple.clinic.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.WindowManager
import com.f2prateek.rx.preferences2.Preference
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.BuildConfig
import org.simple.clinic.ClinicApp
import org.simple.clinic.R
import org.simple.clinic.home.HomeScreen
import org.simple.clinic.login.applock.AppLockScreen
import org.simple.clinic.login.phone.LoginPhoneScreen
import org.simple.clinic.onboarding.OnboardingScreen
import org.simple.clinic.registration.RegistrationConfig
import org.simple.clinic.registration.phone.RegistrationPhoneScreen
import org.simple.clinic.router.ScreenResultBus
import org.simple.clinic.router.screen.ActivityPermissionResult
import org.simple.clinic.router.screen.ActivityResult
import org.simple.clinic.router.screen.FullScreenKey
import org.simple.clinic.router.screen.FullScreenKeyChanger
import org.simple.clinic.router.screen.NestedKeyChanger
import org.simple.clinic.router.screen.RouterDirection
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.user.UserSession
import org.simple.clinic.widgets.TheActivityLifecycle
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Named

class TheActivity : AppCompatActivity() {

  companion object {
    lateinit var component: TheActivityComponent
    const val KEY_INITIAL_SCREEN = "initialScreen"

    fun intentWithInitialScreen(context: Context, initialScreenKey: FullScreenKey): Intent {
      return Intent(context, TheActivity::class.java)
          .putExtra(KEY_INITIAL_SCREEN, initialScreenKey)
    }
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

  @Inject
  lateinit var registrationConfig: Single<RegistrationConfig>

  lateinit var screenRouter: ScreenRouter

  private val screenResults: ScreenResultBus = ScreenResultBus()

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)
    @Suppress("ConstantConditionIf")
    if (BuildConfig.DISABLE_SCREENSHOT) {
      window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
    }

    // getIntent() returns null from attachBaseContext(), so Flow cannot
    // be initialized with the screen key requested by the caller.
    // It can only be overriden here.
    openInitialScreenSetByCaller()

    lifecycle
        .startWith(TheActivityLifecycle.Started())
        .compose(controller)
        .takeUntil(lifecycle.ofType<TheActivityLifecycle.Destroyed>())
        .subscribe { uiChange -> uiChange(this) }
  }

  override fun onNewIntent(newIntent: Intent) {
    super.onNewIntent(newIntent)
    intent = newIntent
    openInitialScreenSetByCaller()
  }

  private fun openInitialScreenSetByCaller() {
    if (intent.hasExtra(KEY_INITIAL_SCREEN)) {
      screenRouter.clearHistoryAndPush(intent.getParcelableExtra(KEY_INITIAL_SCREEN), RouterDirection.REPLACE)
    }
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
    val outgoingScreenName = if(outgoing == null) "" else outgoing.javaClass.simpleName
    val incomingScreenName = incoming.javaClass.simpleName
    Timber.d("$outgoingScreenName -> $incomingScreenName")
  }

  private fun initialScreenKey(): FullScreenKey {
    return when {
      userSession.isUserLoggedIn() -> HomeScreen.KEY
      hasUserCompletedOnboarding.get().not() -> OnboardingScreen.KEY
      else -> {
        registrationConfig
            .map {
              when (it.isRegistrationEnabled) {
                true -> RegistrationPhoneScreen.KEY
                false -> LoginPhoneScreen.KEY_WITHOUT_OTP
              }
            }
            .blockingGet()
      }
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
}
