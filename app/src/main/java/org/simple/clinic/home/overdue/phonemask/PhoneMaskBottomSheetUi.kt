package org.simple.clinic.home.overdue.phonemask

interface PhoneMaskBottomSheetUi {
  fun setupView(patient: PatientDetails)
  fun requestCallPermission()
  fun closeSheet()
  fun hideSecureCallButton()
}
