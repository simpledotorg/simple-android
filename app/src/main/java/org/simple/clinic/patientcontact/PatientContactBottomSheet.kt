package org.simple.clinic.patientcontact

import android.os.Bundle
import org.simple.clinic.R
import org.simple.clinic.widgets.BottomSheetActivity

class PatientContactBottomSheet: BottomSheetActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.sheet_patientcontact)
  }

  override fun onBackgroundClick() {
    finish()
  }
}
