package org.simple.clinic.facility.change.confirm

import android.os.Bundle
import org.simple.clinic.ClinicApp
import org.simple.clinic.R
import org.simple.clinic.facility.change.confirm.di.ConfirmFacilityChangeComponent
import org.simple.clinic.widgets.BottomSheetActivity

class ConfirmFacilityChangeSheet : BottomSheetActivity() {

  companion object {
    lateinit var component: ConfirmFacilityChangeComponent
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
