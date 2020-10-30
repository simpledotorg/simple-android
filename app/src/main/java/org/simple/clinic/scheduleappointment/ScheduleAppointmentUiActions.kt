package org.simple.clinic.scheduleappointment

import java.time.LocalDate
import java.util.UUID

interface ScheduleAppointmentUiActions {
  fun showManualDateSelector(date: LocalDate)
  fun closeSheet()
  fun openTeleconsultStatusSheet(teleconsultRecordUuid: UUID)
}
