package org.simple.clinic.setup

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import org.simple.clinic.BuildConfig
import org.simple.clinic.ClinicApp
import org.simple.clinic.router.ScreenResultBus
import org.simple.clinic.router.screen.ActivityPermissionResult
import org.simple.clinic.router.screen.ActivityResult
import org.simple.clinic.router.screen.NestedKeyChanger
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.util.LocaleOverrideContextWrapper
import org.simple.clinic.util.unsafeLazy
import java.util.Locale
import javax.inject.Inject

class SetupActivity : AppCompatActivity() {

  companion object {
    lateinit var component: SetupActivityComponent
  }

  @Inject
  lateinit var locale: Locale

  private val screenResults = ScreenResultBus()

  private val screenRouter by unsafeLazy {
    ScreenRouter.create(this, NestedKeyChanger(), screenResults)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    @Suppress("ConstantConditionIf")
    if (BuildConfig.DISABLE_SCREENSHOT) {
      window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
    }
  }

  override fun attachBaseContext(baseContext: Context) {
    setupDiGraph()
    val contextWithOverriddenLocale = LocaleOverrideContextWrapper.wrap(baseContext, locale)
    super.attachBaseContext(ViewPumpContextWrapper.wrap(contextWithOverriddenLocale))
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

  private fun setupDiGraph() {
    component = ClinicApp.appComponent
        .setupActivityComponentBuilder()
        .activity(this)
        .screenRouter(screenRouter)
        .build()
    component.inject(this)
  }
}
