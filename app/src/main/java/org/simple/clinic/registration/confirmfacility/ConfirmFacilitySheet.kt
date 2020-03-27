package org.simple.clinic.registration.confirmfacility

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.simple.clinic.R
import org.simple.clinic.widgets.BottomSheetActivity
import java.util.UUID

class ConfirmFacilitySheet : BottomSheetActivity() {

  companion object {
    private const val EXTRA_FACILITY_UUID = "extra_facility_uuid"
    private const val EXTRA_FACILITY_NAME = "extra_facility_name"

    fun intentForConfirmFacilitySheet(
        context: Context,
        facilityUuid: UUID,
        facilityName: String
    ): Intent {
      val intent = Intent(context, ConfirmFacilitySheet::class.java)
      intent.putExtra(EXTRA_FACILITY_UUID, facilityUuid)
      intent.putExtra(EXTRA_FACILITY_NAME, facilityName)
      return intent
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.sheet_registration_confirm_facility)
  }
}
