package org.simple.clinic.teleconsultlog.prescription.doctorinfo

import org.simple.clinic.user.User

interface TeleconsultDoctorInfoUi {
  fun renderDoctorAcknowledgement(user: User)
}
