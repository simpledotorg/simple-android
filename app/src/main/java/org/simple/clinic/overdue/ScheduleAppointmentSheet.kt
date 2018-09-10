package org.simple.clinic.overdue

import android.os.Bundle
import org.simple.clinic.R
import org.simple.clinic.widgets.BottomSheetActivity

class ScheduleAppointmentSheet : BottomSheetActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.sheet_schedule_appointment)
  }
}
