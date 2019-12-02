package org.simple.clinic.scheduleappointment

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import org.simple.clinic.ClinicApp
import org.simple.clinic.R
import org.simple.clinic.util.LocaleOverrideContextWrapper
import java.util.Locale
import javax.inject.Inject

class PatientFacilityChangeActivity : AppCompatActivity() {

  @Inject
  lateinit var locale: Locale

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.screen_facility_change)
  }

  override fun attachBaseContext(baseContext: Context) {
    ClinicApp.appComponent.inject(this)
    val contextWithOverridenLocale = LocaleOverrideContextWrapper.wrap(baseContext, locale)
    super.attachBaseContext(ViewPumpContextWrapper.wrap(contextWithOverridenLocale))
  }
}
