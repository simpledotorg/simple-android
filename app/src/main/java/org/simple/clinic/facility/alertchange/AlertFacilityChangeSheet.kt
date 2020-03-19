package org.simple.clinic.facility.alertchange

import android.content.Context
import android.content.Intent
import android.os.Bundle
import kotlinx.android.synthetic.main.sheet_alert_facility_change.*
import org.simple.clinic.R
import org.simple.clinic.facility.Facility
import org.simple.clinic.widgets.BottomSheetActivity

class AlertFacilityChangeSheet : BottomSheetActivity() {

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

  private fun closeSheet() {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  private fun openFacilityChangeScreen() {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }
}
