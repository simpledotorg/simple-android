package org.simple.clinic

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import org.simple.clinic.feature.Features
import org.simple.clinic.util.withLocale
import java.util.Locale
import javax.inject.Inject

/* Remember to enable the activity in the debug manifest when running this activity. */
class WebviewTestActivity : AppCompatActivity() {

  @Inject
  lateinit var features: Features

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(WebView(this).apply {
      layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
    })
  }

  override fun attachBaseContext(baseContext: Context) {
    DebugClinicApp.appComponent().inject(this)
    super.attachBaseContext(baseContext)
    applyOverrideConfiguration(Configuration())
  }

  override fun applyOverrideConfiguration(overrideConfiguration: Configuration) {
    super.applyOverrideConfiguration(overrideConfiguration.withLocale(Locale.UK, features))
  }
}
