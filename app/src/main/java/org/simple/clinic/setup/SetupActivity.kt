package org.simple.clinic.setup

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import io.reactivex.Observable
import org.simple.clinic.BuildConfig
import org.simple.clinic.ClinicApp
import org.simple.clinic.R
import org.simple.clinic.activity.placeholder.PlaceholderScreenKey
import org.simple.clinic.analytics.Analytics
import org.simple.clinic.di.InjectorProviderContextWrapper
import org.simple.clinic.main.TheActivity
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.onboarding.OnboardingScreenKey
import org.simple.clinic.router.ScreenResultBus
import org.simple.clinic.router.screen.ActivityPermissionResult
import org.simple.clinic.router.screen.ActivityResult
import org.simple.clinic.router.screen.FullScreenKey
import org.simple.clinic.router.screen.FullScreenKeyChanger
import org.simple.clinic.router.screen.NestedKeyChanger
import org.simple.clinic.router.screen.RouterDirection
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.selectcountry.SelectCountryScreenKey
import org.simple.clinic.util.LocaleOverrideContextWrapper
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.util.wrap
import java.util.Locale
import javax.inject.Inject

class SetupActivity : AppCompatActivity(), UiActions {

  @Inject
  lateinit var locale: Locale

  private lateinit var component: SetupActivityComponent

  @Inject
  lateinit var effectHandlerFactory: SetupActivityEffectHandler.Factory

  private val screenResults = ScreenResultBus()

  private val screenRouter by unsafeLazy {
    ScreenRouter.create(this, NestedKeyChanger(), screenResults)
  }

  private val delegate by unsafeLazy {
    MobiusDelegate.forActivity(
        events = Observable.never<SetupActivityEvent>(),
        defaultModel = SetupActivityModel.SETTING_UP,
        update = SetupActivityUpdate(),
        effectHandler = effectHandlerFactory.create(this).build(),
        init = SetupActivityInit(),
        modelUpdateListener = { /* Nothing to do here */ }
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
        .wrap { InjectorProviderContextWrapper.wrap(it, component) }
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
    screenRouter.popAndPush(OnboardingScreenKey(), RouterDirection.FORWARD)
  }

  override fun showCountrySelectionScreen() {
    screenRouter.popAndPush(SelectCountryScreenKey(), RouterDirection.FORWARD)
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
