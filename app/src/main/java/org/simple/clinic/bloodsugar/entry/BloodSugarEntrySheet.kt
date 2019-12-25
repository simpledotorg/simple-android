package org.simple.clinic.bloodsugar.entry

import android.os.Bundle
import org.simple.clinic.R
import org.simple.clinic.widgets.BottomSheetActivity

class BloodSugarEntrySheet : BottomSheetActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.sheet_blood_sugar_entry)
  }
}
