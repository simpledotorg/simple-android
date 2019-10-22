package org.simple.clinic.setup

import android.content.Context
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import org.simple.clinic.BuildConfig
import org.simple.clinic.ClinicApp
import org.simple.clinic.util.LocaleOverrideContextWrapper
import java.util.Locale
import javax.inject.Inject

class SetupActivity : AppCompatActivity() {

  companion object {
    lateinit var component: SetupActivityComponent
  }

  @Inject
  lateinit var locale: Locale

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

  private fun setupDiGraph() {
    component = ClinicApp.appComponent
        .setupActivityComponentBuilder()
        .activity(this)
        .build()
    component.inject(this)
  }
}
