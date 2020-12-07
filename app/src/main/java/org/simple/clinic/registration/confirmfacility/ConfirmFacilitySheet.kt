package org.simple.clinic.registration.confirmfacility

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.simple.clinic.databinding.SheetRegistrationConfirmFacilityBinding
import org.simple.clinic.util.unsafeLazy
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

    fun confirmedFacilityUuid(intent: Intent): UUID {
      return intent.getSerializableExtra(EXTRA_FACILITY_UUID) as UUID
    }
  }

  private val facilityName: String by unsafeLazy {
    intent.getStringExtra(EXTRA_FACILITY_NAME)!!
  }

  private val facilityUuid: UUID by unsafeLazy {
    intent.getSerializableExtra(EXTRA_FACILITY_UUID) as UUID
  }

  private lateinit var binding: SheetRegistrationConfirmFacilityBinding

  private val facilityNameTextView
    get() = binding.facilityNameTextView

  private val yesButton
    get() = binding.yesButton

  private val cancelButton
    get() = binding.cancelButton

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    binding = SheetRegistrationConfirmFacilityBinding.inflate(layoutInflater)
    setContentView(binding.root)

    facilityNameTextView.text = facilityName

    yesButton.setOnClickListener {
      confirmFacilitySelection()
    }

    cancelButton.setOnClickListener {
      finish()
    }
  }

  private fun confirmFacilitySelection() {
    val intent = Intent()
    intent.putExtra(EXTRA_FACILITY_UUID, facilityUuid)
    setResult(Activity.RESULT_OK, intent)
    finish()
  }
}
