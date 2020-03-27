package org.simple.clinic.facility.alertchange

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import kotlinx.android.synthetic.main.sheet_alert_facility_change.*
import org.simple.clinic.ClinicApp
import org.simple.clinic.R
import org.simple.clinic.facility.change.FacilityChangeActivity
import org.simple.clinic.util.LocaleOverrideContextWrapper
import org.simple.clinic.util.wrap
import org.simple.clinic.widgets.BottomSheetActivity
import java.util.Locale
import javax.inject.Inject

class AlertFacilityChangeSheet : BottomSheetActivity() {

  @Inject
  lateinit var locale: Locale

  private lateinit var component: AlertFacilityChangeComponent

  companion object {
    const val FACILITY_CHANGE = 101
    private const val CURRENT_FACILITY_NAME = "current_facility"

    fun intent(context: Context, currentFacilityName: String): Intent {
      val intent = Intent(context, AlertFacilityChangeSheet::class.java)
      intent.putExtra(CURRENT_FACILITY_NAME, currentFacilityName)
      return intent
    }
  }

  private val currentFacilityName by lazy {
    intent.getStringExtra(CURRENT_FACILITY_NAME)!!
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.sheet_alert_facility_change)

    facilityName.text = getString(R.string.alertfacilitychange_facility_name, currentFacilityName)
    yesButton.setOnClickListener {
      closeSheet(Activity.RESULT_OK)
    }

    changeButton.setOnClickListener {
      openFacilityChangeScreen()
    }
  }

  override fun attachBaseContext(baseContext: Context) {
    setupDI()

    val wrappedContext = baseContext
        .wrap { LocaleOverrideContextWrapper.wrap(it, locale) }
        .wrap { ViewPumpContextWrapper.wrap(it) }

    super.attachBaseContext(wrappedContext)
  }

  private fun setupDI() {
    component = ClinicApp.appComponent
        .alertFacilityChangeComponent()
        .activity(this)
        .build()
    component.inject(this)
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)

    if (requestCode == FACILITY_CHANGE) {
      closeSheet(resultCode)
    }
  }

  private fun closeSheet(resultCode: Int) {
    val intent = Intent()
    setResult(resultCode, intent)
    finish()
  }

  private fun openFacilityChangeScreen() {
    startActivityForResult(FacilityChangeActivity.intent(this), FACILITY_CHANGE)
  }
}
