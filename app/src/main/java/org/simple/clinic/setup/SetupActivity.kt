package org.simple.clinic.setup

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import io.reactivex.Observable
import org.simple.clinic.BuildConfig
import org.simple.clinic.ClinicApp
import org.simple.clinic.R
import org.simple.clinic.activity.placeholder.PlaceholderScreen.PlaceHolderScreenKey
import org.simple.clinic.databinding.ActivitySetupBinding
import org.simple.clinic.di.InjectorProviderContextWrapper
import org.simple.clinic.feature.Features
import org.simple.clinic.main.TheActivity
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.compat.wrap
import org.simple.clinic.registerorlogin.AuthenticationActivity
import org.simple.clinic.router.ScreenResultBus
import org.simple.clinic.router.screen.ActivityPermissionResult
import org.simple.clinic.router.screen.ActivityResult
import org.simple.clinic.setup.runcheck.Disallowed
import org.simple.clinic.splash.SplashScreen.SplashScreenKey
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.disableAnimations
import org.simple.clinic.util.disablePendingTransitions
import org.simple.clinic.util.finishWithoutAnimations
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.util.withLocale
import org.simple.clinic.util.wrap
import java.util.Locale
import javax.inject.Inject

class SetupActivity : AppCompatActivity(), UiActions {

  @Inject
  lateinit var locale: Locale

  private lateinit var component: SetupActivityComponent

  @Inject
  lateinit var effectHandlerFactory: SetupActivityEffectHandler.Factory

  @Inject
  lateinit var config: SetupActivityConfig

  @Inject
  lateinit var clock: UtcClock

  @Inject
  lateinit var features: Features

  private val screenResults = ScreenResultBus()

  private val router by unsafeLazy {
    Router(
        initialScreenKey = PlaceHolderScreenKey.wrap(),
        fragmentManager = supportFragmentManager,
        containerId = R.id.screen_host_view
    )
  }

  private val delegate by unsafeLazy {
    MobiusDelegate.forActivity(
        events = Observable.never(),
        defaultModel = SetupActivityModel.create(clock),
        update = SetupActivityUpdate(config),
        effectHandler = effectHandlerFactory.create(this).build(),
        init = SetupActivityInit(),
        modelUpdateListener = { /* Nothing to do here */ }
    )
  }

  private lateinit var binding: ActivitySetupBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    @Suppress("ConstantConditionIf")
    if (BuildConfig.DISABLE_SCREENSHOT) {
      window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
    }

    binding = ActivitySetupBinding.inflate(layoutInflater)
    setContentView(binding.root)

    router.onReady(savedInstanceState)
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
    router.onSaveInstanceState(outState)
    delegate.onSaveInstanceState(outState)
    super.onSaveInstanceState(outState)
  }

  override fun onBackPressed() {
    if (!router.onBackPressed()) {
      super.onBackPressed()
    }
  }

  override fun attachBaseContext(baseContext: Context) {
    setupDiGraph()

    val wrappedContext = baseContext
        .wrap { InjectorProviderContextWrapper.wrap(it, component) }
        .wrap { ViewPumpContextWrapper.wrap(it) }

    super.attachBaseContext(wrappedContext)
    applyOverrideConfiguration(Configuration())
  }

  override fun applyOverrideConfiguration(overrideConfiguration: Configuration) {
    super.applyOverrideConfiguration(overrideConfiguration.withLocale(locale, features))
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    screenResults.send(ActivityResult(requestCode, resultCode, data))
  }

  override fun onRequestPermissionsResult(
      requestCode: Int,
      permissions: Array<out String>,
      grantResults: IntArray
  ) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    screenResults.send(ActivityPermissionResult(requestCode))
  }

  override fun goToMainActivity() {
    val intent = TheActivity
        .newIntent(this, isFreshAuthentication = false)
        .apply {
          flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION
        }
    startActivity(intent)
    disablePendingTransitions()
  }

  override fun showSplashScreen() {
    navigateToSplashScreen()
  }

  private fun navigateToSplashScreen() {
    router.clearHistoryAndPush(SplashScreenKey.wrap())
  }

  override fun showCountrySelectionScreen() {
    val intent = AuthenticationActivity
        .forNewLogin(this)
        .disableAnimations()

    startActivity(intent)
    finishWithoutAnimations()
  }

  override fun showDisallowedToRunError(reason: Disallowed.Reason) {
    val dialog = MaterialAlertDialogBuilder(this)
        .setTitle(R.string.setup_cannot_run)
        .setMessage(R.string.setup_rooted)
        .setCancelable(false)
        .setPositiveButton(R.string.setup_close) { _, _ -> finish() }

    dialog.show()
  }

  private fun setupDiGraph() {
    component = ClinicApp.appComponent
        .setupActivityComponent()
        .create(
            activity = this,
            router = router
        )

    component.inject(this)
  }
}
