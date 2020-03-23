package org.simple.clinic.facility.alertchange

import android.content.Context
import android.content.Intent
import android.os.Bundle
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import kotlinx.android.synthetic.main.sheet_alert_facility_change.*
import org.simple.clinic.ClinicApp
import org.simple.clinic.R
import org.simple.clinic.facility.Facility
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
    private const val CURRENT_FACILITY = "current_facility"

    fun intent(context: Context, currentFacility: Facility): Intent {
      val intent = Intent(context, AlertFacilityChangeSheet::class.java)
      intent.putExtra(CURRENT_FACILITY, currentFacility)
      return intent
    }
  }

  private val currentFacility by lazy {
    intent.getParcelableExtra<Facility>(CURRENT_FACILITY)!!
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.sheet_alert_facility_change)

    facilityName.text = getString(R.string.alertfacilitychange_facility_name, currentFacility.name)
    yesButton.setOnClickListener {
      closeSheet()
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

  private fun closeSheet() {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  private fun openFacilityChangeScreen() {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }
}
