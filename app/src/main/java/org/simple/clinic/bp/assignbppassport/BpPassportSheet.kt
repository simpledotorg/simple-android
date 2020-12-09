package org.simple.clinic.bp.assignbppassport

import android.os.Bundle
import org.simple.clinic.R
import org.simple.clinic.widgets.BottomSheetActivity

class BpPassportSheet : BottomSheetActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.sheet_bp_passport)
  }
}
