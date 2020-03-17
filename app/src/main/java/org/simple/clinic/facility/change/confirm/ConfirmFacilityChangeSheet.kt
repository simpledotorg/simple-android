package org.simple.clinic.facility.change.confirm

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.simple.clinic.ClinicApp
import org.simple.clinic.R
import org.simple.clinic.facility.Facility
import org.simple.clinic.facility.change.confirm.di.ConfirmFacilityChangeComponent
import org.simple.clinic.widgets.BottomSheetActivity

class ConfirmFacilityChangeSheet : BottomSheetActivity() {

  companion object {
    lateinit var component: ConfirmFacilityChangeComponent

    private const val SELECTED_FACILITY = "seleceted_facility"

    fun intent(
        context: Context,
        facility: Facility
    ): Intent {
      val intent = Intent(context, ConfirmFacilityChangeSheet::class.java)
      intent.putExtra(SELECTED_FACILITY, facility)
      return intent
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.sheet_confirm_facility_change)
    setupDi()
  }

  private fun setupDi() {
    component = ClinicApp.appComponent
        .confirmFacilityChangeComponent()
        .activity(this)
        .build()

    component.inject(this)
  }
}
