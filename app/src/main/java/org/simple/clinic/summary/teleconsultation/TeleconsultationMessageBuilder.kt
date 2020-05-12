package org.simple.clinic.summary.teleconsultation

import android.content.res.Resources
import org.simple.clinic.R
import org.simple.clinic.summary.PatientTeleconsultationInfo
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.toLocalDateAtZone
import org.threeten.bp.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Named

class TeleconsultationMessageBuilder @Inject constructor(
    private val resources: Resources,
    private val userClock: UserClock,
    @Named("full_date") private val dateFormatter: DateTimeFormatter
) {

  fun message(patientTeleconsultationInfo: PatientTeleconsultationInfo): String {
    val message = StringBuilder("*${patientTeleconsultationInfo.facility.name}* teleconsult request for:")
        .appendln("")
        .appendln("")
        .appendln("*Patient record*:")
        .appendln("https://app.simple.org/patient/${patientTeleconsultationInfo.patientUuid}")
        .appendln("")

    if (patientTeleconsultationInfo.bloodPressures.isNotEmpty()) {
      val bloodPressures = patientTeleconsultationInfo
          .bloodPressures.joinToString(separator = "\n") {
            val bpRecordedAtDate = dateFormatter.format(it.recordedAt.toLocalDateAtZone(userClock.zone))
            "${it.reading.systolic}/${it.reading.diastolic} ($bpRecordedAtDate)"
          }
      val bloodPressuresSize = patientTeleconsultationInfo.bloodPressures.size
      val bloodPressuresTitle = resources
          .getQuantityString(R.plurals.patientsummary_contact_doctor_patient_info_bps, bloodPressuresSize, bloodPressuresSize.toString())

      message.appendln(bloodPressuresTitle)
          .appendln(bloodPressures)
          .appendln("")
    }

    if (patientTeleconsultationInfo.prescriptions.isNotEmpty()) {
      val medicines = patientTeleconsultationInfo
          .prescriptions.joinToString(separator = "\n") { "${it.name} ${it.dosage}" }
      message.appendln("*Current medicines*:")
          .appendln(medicines)
          .appendln("")
    }

    if (patientTeleconsultationInfo.bpPassport.isNullOrBlank().not()) {
      message.appendln("*BP Passport*: ${patientTeleconsultationInfo.bpPassport}")
          .appendln("")
    }

    return message.toString()
  }
}
