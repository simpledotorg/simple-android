package org.simple.clinic.registration.confirmfacility

import android.os.Bundle
import org.simple.clinic.R
import org.simple.clinic.widgets.BottomSheetActivity

class ConfirmFacilitySheet : BottomSheetActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.sheet_registration_confirm_facility)
  }
}
