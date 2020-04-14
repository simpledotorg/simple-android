package org.simple.clinic.contactpatient

import org.simple.clinic.patient.Gender

interface ContactPatientUi {
  fun renderPatientDetails(name: String, gender: Gender, age: Int, phoneNumber: String)

  fun showCallResultSection()
  fun hideCallResultSection()

  fun showSecureCallUi()
  fun hideSecureCallUi()
}
