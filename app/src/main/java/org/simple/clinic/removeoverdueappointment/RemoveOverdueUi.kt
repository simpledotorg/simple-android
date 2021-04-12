package org.simple.clinic.removeoverdueappointment

import org.simple.clinic.contactpatient.RemoveAppointmentReason

interface RemoveOverdueUi {
  fun renderAppointmentRemoveReasons(reasons: List<RemoveAppointmentReason>, selectedReason: RemoveAppointmentReason?)
  fun disableDoneButton()
  fun enableDoneButton()
}
