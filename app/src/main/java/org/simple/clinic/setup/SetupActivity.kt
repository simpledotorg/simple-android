package org.simple.clinic.setup

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.f2prateek.rx.preferences2.Preference
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import io.reactivex.Observable
import org.simple.clinic.BuildConfig
import org.simple.clinic.ClinicApp
import org.simple.clinic.R
import org.simple.clinic.activity.placeholder.PlaceholderScreenKey
import org.simple.clinic.analytics.Analytics
import org.simple.clinic.di.InjectorProviderContextWrapper
import org.simple.clinic.main.TheActivity
import org.simple.clinic.mobius.MobiusActivityDelegate
import org.simple.clinic.onboarding.OnboardingScreenInjector
import org.simple.clinic.onboarding.OnboardingScreenKey
import org.simple.clinic.platform.crash.CrashReporter
import org.simple.clinic.router.ScreenResultBus
import org.simple.clinic.router.screen.ActivityPermissionResult
import org.simple.clinic.router.screen.ActivityResult
import org.simple.clinic.router.screen.FullScreenKey
import org.simple.clinic.router.screen.FullScreenKeyChanger
import org.simple.clinic.router.screen.NestedKeyChanger
import org.simple.clinic.router.screen.RouterDirection
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.util.LocaleOverrideContextWrapper
import org.simple.clinic.util.scheduler.SchedulersProvider
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.util.wrap
import java.util.Locale
import javax.inject.Inject
import javax.inject.Named

class SetupActivity : AppCompatActivity(), UiActions {

  @Inject
  lateinit var locale: Locale

  @field:[Inject Named("onboarding_complete")]
  lateinit var onboardingCompletePreference: Preference<Boolean>

  @Inject
  lateinit var schedulersProvider: SchedulersProvider

  @Inject
  lateinit var crashReporter: CrashReporter

  private lateinit var component: SetupActivityComponent

  private val screenResults = ScreenResultBus()

  private val screenRouter by unsafeLazy {
    ScreenRouter.create(this, NestedKeyChanger(), screenResults)
  }

  private val delegate by unsafeLazy {
    val effectHandler = SetupActivityEffectHandler.create(onboardingCompletePreference, this, schedulersProvider)

    MobiusActivityDelegate(
        events = Observable.never<SetupActivityEvent>(),
        defaultModel = SetupActivityModel,
        init = SetupActivityInit(),
        update = SetupActivityUpdate(),
        effectHandler = effectHandler,
        modelUpdateListener = { /* Nothing to do here */ },
        crashReporter = crashReporter
    )
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    @Suppress("ConstantConditionIf")
    if (BuildConfig.DISABLE_SCREENSHOT) {
      window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
    }

    delegate.onRestoreInstanceState(savedInstanceState)
  }

  override fun onStart() {
    super.onStart()
    delegate.start()
  }

  override fun onStop() {
    delegate.stop()
    super.onStop()
  }

  override fun onSaveInstanceState(outState: Bundle) {
    delegate.onSaveInstanceState(outState)
    super.onSaveInstanceState(outState)
  }

  override fun attachBaseContext(baseContext: Context) {
    setupDiGraph()

    val wrappedContext = baseContext
        .wrap { LocaleOverrideContextWrapper.wrap(it, locale) }
        .wrap { wrapContextWithRouter(it) }
        .wrap { InjectorProviderContextWrapper.wrap(it, mapOf(OnboardingScreenInjector::class.java to component)) }
        .wrap { ViewPumpContextWrapper.wrap(it) }

    super.attachBaseContext(wrappedContext)
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

  override fun goToMainActivity() {
    val intent = TheActivity.newIntent(this).apply {
      flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION
    }
    startActivity(intent)
    overridePendingTransition(0, 0)
  }

  override fun showOnboardingScreen() {
    screenRouter.popAndPush(OnboardingScreenKey(migrated = true), RouterDirection.FORWARD)
  }

  private fun wrapContextWithRouter(baseContext: Context): Context {
    screenRouter.registerKeyChanger(FullScreenKeyChanger(
        activity = this,
        screenLayoutContainerRes = android.R.id.content,
        screenBackgroundRes = R.color.window_background,
        onKeyChange = this::onScreenChanged
    ))
    return screenRouter.installInContext(baseContext, PlaceholderScreenKey())
  }

  private fun onScreenChanged(outgoing: FullScreenKey?, incoming: FullScreenKey) {
    val outgoingScreenName = outgoing?.analyticsName ?: ""
    val incomingScreenName = incoming.analyticsName
    Analytics.reportScreenChange(outgoingScreenName, incomingScreenName)
  }

  private fun setupDiGraph() {
    component = ClinicApp.appComponent
        .setupActivityComponentBuilder()
        .activity(this)
        .screenRouter(screenRouter)
        .build()
    component.inject(this)
  }
}
