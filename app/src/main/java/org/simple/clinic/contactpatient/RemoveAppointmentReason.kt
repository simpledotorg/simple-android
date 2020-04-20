package org.simple.clinic.contactpatient

import androidx.annotation.StringRes
import org.simple.clinic.R

enum class RemoveAppointmentReason(@StringRes val displayText: Int) {
  AlreadyVisited(R.string.contactpatient_patient_already_visited),
  NotResponding(R.string.contactpatient_patient_is_not_responding),
  PhoneNumberNotWorking(R.string.contactpatient_invalid_phone_number),
  TransferredToAnotherFacility(R.string.contactpatient_public_hospital_transfer),
  MovedToPrivatePractitioner(R.string.contactpatient_moved_to_private),
  Died(R.string.contactpatient_patient_died),
  OtherReason(R.string.contactpatient_other_reason)
}
