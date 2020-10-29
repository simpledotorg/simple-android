package org.simple.clinic.registerorlogin

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import io.reactivex.Observable
import org.simple.clinic.ClinicApp
import org.simple.clinic.R
import org.simple.clinic.di.InjectorProviderContextWrapper
import org.simple.clinic.empty.EmptyScreenKey
import org.simple.clinic.feature.Feature
import org.simple.clinic.feature.Features
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.platform.analytics.Analytics
import org.simple.clinic.registration.phone.RegistrationPhoneScreenKey
import org.simple.clinic.router.ScreenResultBus
import org.simple.clinic.router.screen.ActivityPermissionResult
import org.simple.clinic.router.screen.ActivityResult
import org.simple.clinic.router.screen.FullScreenKey
import org.simple.clinic.router.screen.FullScreenKeyChanger
import org.simple.clinic.router.screen.NestedKeyChanger
import org.simple.clinic.router.screen.RouterDirection
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.selectcountry.SelectCountryScreenKey
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.util.withLocale
import org.simple.clinic.util.wrap
import java.util.Locale
import javax.inject.Inject

class AuthenticationActivity : AppCompatActivity(), AuthenticationUiActions {

  companion object {
    private const val EXTRA_OPEN_FOR = "AuthenticationActivity.EXTRA_OPEN_FOR"

    fun forNewLogin(context: Context): Intent {
      return Intent(context, AuthenticationActivity::class.java).apply {
        putExtra(EXTRA_OPEN_FOR, OpenFor.NewLogin)
      }
    }

    fun forReauthentication(context: Context): Intent {
      return Intent(context, AuthenticationActivity::class.java).apply {
        putExtra(EXTRA_OPEN_FOR, OpenFor.Reauthentication)
      }
    }
  }

  @Inject
  lateinit var locale: Locale

  @Inject
  lateinit var features: Features

  @Inject
  lateinit var effectHandlerFactory: AuthenticationEffectHandler.Factory

  private val screenRouter: ScreenRouter by unsafeLazy {
    ScreenRouter.create(this, NestedKeyChanger(), screenResults)
  }

  private val screenResults: ScreenResultBus = ScreenResultBus()

  private val openFor: OpenFor by unsafeLazy {
    intent.extras!!.getSerializable(EXTRA_OPEN_FOR)!! as OpenFor
  }

  private val delegate: MobiusDelegate<AuthenticationModel, AuthenticationEvent, AuthenticationEffect> by unsafeLazy {
    MobiusDelegate.forActivity(
        events = Observable.never(),
        effectHandler = effectHandlerFactory.create(this).build(),
        init = AuthenticationInit(),
        defaultModel = AuthenticationModel.create(openFor)
    )
  }

  private lateinit var component: AuthenticationActivityComponent

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    delegate.onRestoreInstanceState(savedInstanceState)
  }

  override fun attachBaseContext(baseContext: Context) {
    setupDiGraph()

    val wrappedContext = baseContext
        .wrap { wrapContextWithRouter(it) }
        .wrap { InjectorProviderContextWrapper.wrap(it, component) }
        .wrap { ViewPumpContextWrapper.wrap(it) }

    super.attachBaseContext(wrappedContext)
    applyOverrideConfiguration(Configuration())
  }

  override fun applyOverrideConfiguration(overrideConfiguration: Configuration) {
    super.applyOverrideConfiguration(overrideConfiguration.withLocale(locale))
  }

  override fun onStart() {
    super.onStart()
    delegate.start()
  }

  override fun onStop() {
    delegate.stop()
    super.onStop()
  }

  private fun setupDiGraph() {
    component = ClinicApp.appComponent
        .authenticationActivityComponent()
        .activity(this)
        .screenRouter(screenRouter)
        .build()
    component.inject(this)
  }

  private fun wrapContextWithRouter(baseContext: Context): Context {
    screenRouter.registerKeyChanger(FullScreenKeyChanger(
        activity = this,
        screenLayoutContainerRes = android.R.id.content,
        screenBackgroundRes = R.color.window_background,
        onKeyChange = ::onScreenChanged
    ))

    return screenRouter.installInContext(baseContext, EmptyScreenKey())
  }

  private fun onScreenChanged(outgoing: FullScreenKey?, incoming: FullScreenKey) {
    val outgoingScreenName = outgoing?.analyticsName ?: ""
    val incomingScreenName = incoming.analyticsName
    Analytics.reportScreenChange(outgoingScreenName, incomingScreenName)
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

  override fun onSaveInstanceState(outState: Bundle) {
    if (features.isEnabled(Feature.LogSavedStateSizes)) {
      screenRouter.logSizesOfSavedStates()
    }
    delegate.onSaveInstanceState(outState)
    super.onSaveInstanceState(outState)
  }

  override fun openCountrySelectionScreen() {
    screenRouter.clearHistoryAndPush(SelectCountryScreenKey(), RouterDirection.REPLACE)
  }

  override fun openRegistrationPhoneScreen() {
    screenRouter.clearHistoryAndPush(RegistrationPhoneScreenKey(), RouterDirection.REPLACE)
  }
}
