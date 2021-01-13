package org.simple.clinic.scanid

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import org.simple.clinic.ClinicApp
import org.simple.clinic.databinding.ScreenScanSimpleBinding
import org.simple.clinic.di.InjectorProviderContextWrapper
import org.simple.clinic.feature.Features
import org.simple.clinic.util.withLocale
import org.simple.clinic.util.wrap
import java.util.Locale
import javax.inject.Inject

class ScanBpPassportActivity : AppCompatActivity(), ScanSimpleIdScreen.ScanResultsReceiver {

  companion object {
    private const val SCAN_RESULT = "org.simple.clinic.scanid.ScanBpPassportActivity.SCAN_RESULT"

    fun readScannedId(data: Intent): ScanResult {
      return data.getParcelableExtra(SCAN_RESULT)!!
    }

    fun intent(context: Context): Intent {
      return Intent(context, ScanBpPassportActivity::class.java)
    }
  }

  @Inject
  lateinit var locale: Locale

  @Inject
  lateinit var features: Features

  @Inject
  lateinit var component: ScanBpPassportActivityComponent

  private lateinit var binding: ScreenScanSimpleBinding

  private val scanBpPassportView
    get() = binding.scanBpPassportView

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    binding = ScreenScanSimpleBinding.inflate(layoutInflater)
    setContentView(binding.root)
    scanBpPassportView.scanResultsReceiver = this
  }

  override fun onScanResult(scanResult: ScanResult) {
    val resultIntent = Intent().apply {
      putExtra(SCAN_RESULT, scanResult)
    }

    setResult(RESULT_OK, resultIntent)
    finish()
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

  private fun setupDiGraph() {
    component = ClinicApp.appComponent
        .scanBpPassportActivityComponent()
        .create(activity = this)

    component.inject(this)
  }
}
