package org.simple.clinic.setup

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import io.reactivex.Observable
import org.simple.clinic.BuildConfig
import org.simple.clinic.ClinicApp
import org.simple.clinic.R
import org.simple.clinic.di.InjectorProviderContextWrapper
import org.simple.clinic.main.TheActivity
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.registerorlogin.AuthenticationActivity
import org.simple.clinic.router.ScreenResultBus
import org.simple.clinic.router.screen.ActivityPermissionResult
import org.simple.clinic.router.screen.ActivityResult
import org.simple.clinic.setup.runcheck.Disallowed
import org.simple.clinic.util.LocaleOverrideContextWrapper
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.disableAnimations
import org.simple.clinic.util.disablePendingTransitions
import org.simple.clinic.util.finishWithoutAnimations
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

  @Inject
  lateinit var config: SetupActivityConfig

  @Inject
  lateinit var clock: UtcClock

  private val screenResults = ScreenResultBus()

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

  private lateinit var navController: NavController

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    @Suppress("ConstantConditionIf")
    if (BuildConfig.DISABLE_SCREENSHOT) {
      window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
    }
    setContentView(R.layout.activity_setup)
    navController = findNavController(R.id.screen_host_view)

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
    if (navController.currentDestination?.id == R.id.placeholderScreen &&
        navController.currentDestination?.id != R.id.splashScreen) {
      navController.navigate(R.id.action_placeholderScreen_to_splashScreen)
    }
  }

  override fun showCountrySelectionScreen() {
    val intent = AuthenticationActivity
        .forNewLogin(this)
        .disableAnimations()

    startActivity(intent)
    finishWithoutAnimations()
  }

  override fun showDisallowedToRunError(reason: Disallowed.Reason) {
    val dialog = AlertDialog
        .Builder(this, R.style.Clinic_V2_DialogStyle)
        .setTitle(R.string.setup_cannot_run)
        .setMessage(R.string.setup_rooted)
        .setCancelable(false)
        .setPositiveButton(R.string.setup_close) { _, _ -> finish() }

    dialog.show()
  }

  private fun setupDiGraph() {
    component = ClinicApp.appComponent
        .setupActivityComponentBuilder()
        .activity(this)
        .build()
    component.inject(this)
  }
}
