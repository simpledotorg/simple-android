package org.simple.clinic.scanid

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import org.simple.clinic.ClinicApp
import org.simple.clinic.R
import org.simple.clinic.di.InjectorProviderContextWrapper
import org.simple.clinic.util.LocaleOverrideContextWrapper
import org.simple.clinic.util.wrap
import java.util.Locale
import javax.inject.Inject

class ScanBpPassportActivity: AppCompatActivity() {

  @Inject
  lateinit var locale: Locale

  @Inject
  lateinit var component: ScanBpPassportActivityComponent

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.screen_scan_simple)
  }

  override fun attachBaseContext(baseContext: Context) {
    setupDiGraph()

    val wrappedContext = baseContext
        .wrap { LocaleOverrideContextWrapper.wrap(it, locale) }
        .wrap { InjectorProviderContextWrapper.wrap(it, component) }
        .wrap { ViewPumpContextWrapper.wrap(it) }

    super.attachBaseContext(wrappedContext)
  }

  private fun setupDiGraph() {
    component = ClinicApp.appComponent
        .scanBpPassportActivityComponent()
        .activity(this)
        .build()

    component.inject(this)
  }
}
