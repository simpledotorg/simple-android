package org.simple.clinic.summary.nextappointment

import java.util.UUID

interface NextAppointmentUiActions {
  fun openScheduleAppointmentSheet(patientUUID: UUID)
}
