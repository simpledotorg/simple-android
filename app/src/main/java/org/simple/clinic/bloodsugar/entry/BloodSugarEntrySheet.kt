package org.simple.clinic.bloodsugar.entry

import android.os.Bundle
import org.simple.clinic.R
import org.simple.clinic.widgets.BottomSheetActivity

class BloodSugarEntrySheet : BottomSheetActivity() {

  enum class ScreenType {
    BLOOD_SUGAR_ENTRY,
    DATE_ENTRY
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.sheet_blood_sugar_entry)
  }
}
