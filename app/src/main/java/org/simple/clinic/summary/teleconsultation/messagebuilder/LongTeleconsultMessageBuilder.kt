package org.simple.clinic.summary.teleconsultation.messagebuilder

import android.content.res.Resources
import org.simple.clinic.R
import org.simple.clinic.summary.PatientTeleconsultationInfo
import org.simple.clinic.summary.PatientTeleconsultationInfoLong
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.toLocalDateAtZone
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Named

class LongTeleconsultMessageBuilder @Inject constructor(
    private val resources: Resources,
    private val userClock: UserClock,
    @Named("full_date") private val dateFormatter: DateTimeFormatter
) : TeleconsultationMessageBuilder {

  companion object {
    private const val LINE_BREAK = "\n"
    private val SECTION_BREAKER = "-".repeat(44)
  }

  /**
   * We are not translating the message because we don't know the preferred language
   * for the recipient.
   */
  override fun message(patientTeleconsultationInfo: PatientTeleconsultationInfo): String {
    val patientTeleconsultationInfoLong = patientTeleconsultationInfo as PatientTeleconsultationInfoLong
    val message = StringBuilder("🔔 *${patientTeleconsultationInfoLong.facility.name}* teleconsult request:")
        .appendLine("")
        .appendLine("")
        .appendLine("1️⃣ Review Patient:")
        .appendLine("")

    if (patientTeleconsultationInfoLong.bpPassport.isNullOrBlank().not()) {
      message.appendLine("*BP Passport*: ${patientTeleconsultationInfoLong.bpPassport}")
          .appendLine("")
    }

    addBloodPressuresSectionToMessage(patientTeleconsultationInfoLong, message)
    addBloodSugarsSectionToMessage(patientTeleconsultationInfoLong, message)
    addPrescriptionsSectionToMessage(patientTeleconsultationInfoLong, message)

    sectionBreaker(message)

    addCallForTeleconsultSectionToMessage(patientTeleconsultationInfoLong, message)

    sectionBreaker(message)

    addLogTeleconsultSectionToMessage(patientTeleconsultationInfoLong, message)

    sectionBreaker(message)

    addCallDoctorSectionToMessage(patientTeleconsultationInfoLong, message)

    sectionBreaker(message)

    return message.toString()
  }

  private fun addCallDoctorSectionToMessage(patientTeleconsultationInfoLong: PatientTeleconsultationInfoLong, message: StringBuilder) {
    message
        .appendLine("*Call doctor*: ${patientTeleconsultationInfoLong.doctorPhoneNumber}")
        .appendLine("")
  }

  private fun addLogTeleconsultSectionToMessage(patientTeleconsultationInfoLong: PatientTeleconsultationInfoLong, message: StringBuilder) {
    message
        .appendLine("3️⃣ *Log teleconsult*: https://app.simple.org/consult?r=${patientTeleconsultationInfoLong.teleconsultationId}&p=${patientTeleconsultationInfoLong.patientUuid}")
        .appendLine("")
  }

  private fun addCallForTeleconsultSectionToMessage(
      patientTeleconsultationInfoLong: PatientTeleconsultationInfoLong,
      message: StringBuilder
  ) {
    if (patientTeleconsultationInfoLong.nursePhoneNumber.isNullOrBlank().not()) {
      message
          .appendLine("2️⃣ *Call for teleconsult*: ${patientTeleconsultationInfoLong.nursePhoneNumber}")
          .appendLine("")
    }
  }

  private fun addPrescriptionsSectionToMessage(
      patientTeleconsultationInfo: PatientTeleconsultationInfoLong,
      message: StringBuilder
  ) {
    if (patientTeleconsultationInfo.prescriptions.isNotEmpty()) {
      val medicines = patientTeleconsultationInfo
          .prescriptions.joinToString(separator = LINE_BREAK) { "${it.name} ${it.dosage}" }
      message.appendLine("*Current medicines*:")
          .appendLine(medicines)
          .appendLine("")
    }
  }

  private fun addBloodSugarsSectionToMessage(
      patientTeleconsultationInfo: PatientTeleconsultationInfoLong,
      message: StringBuilder
  ) {
    if (patientTeleconsultationInfo.bloodSugars.isNotEmpty()) {
      val bloodSugars = patientTeleconsultationInfo
          .bloodSugars.joinToString(separator = LINE_BREAK) {
            val bloodSugarRecordedAtDate = dateFormatter.format(it.recordedAt.toLocalDateAtZone(userClock.zone))
            val bloodSugarType = resources.getString(it.reading.displayType)
            val bloodSugarUnit = resources.getString(it.reading.displayUnit)

            "$bloodSugarType ${it.reading.displayValue}${it.reading.displayUnitSeparator}${bloodSugarUnit} ($bloodSugarRecordedAtDate)"
          }

      val bloodSugarsSize = patientTeleconsultationInfo.bloodSugars.size
      val bloodSugarsTitle = resources
          .getQuantityString(R.plurals.patientsummary_contact_doctor_patient_info_blood_sugars, bloodSugarsSize, bloodSugarsSize.toString())

      message.appendLine(bloodSugarsTitle)
          .appendLine(bloodSugars)
          .appendLine("")
    }
  }

  private fun addBloodPressuresSectionToMessage(
      patientTeleconsultationInfo: PatientTeleconsultationInfoLong,
      message: StringBuilder
  ) {
    if (patientTeleconsultationInfo.bloodPressures.isNotEmpty()) {
      val bloodPressures = patientTeleconsultationInfo
          .bloodPressures.joinToString(separator = LINE_BREAK) {
            val bpRecordedAtDate = dateFormatter.format(it.recordedAt.toLocalDateAtZone(userClock.zone))
            "${it.reading.systolic}/${it.reading.diastolic} ($bpRecordedAtDate)"
          }
      val bloodPressuresSize = patientTeleconsultationInfo.bloodPressures.size
      val bloodPressuresTitle = resources
          .getQuantityString(R.plurals.patientsummary_contact_doctor_patient_info_bps, bloodPressuresSize, bloodPressuresSize.toString())

      message.appendLine(bloodPressuresTitle)
          .appendLine(bloodPressures)
          .appendLine("")
    }
  }

  private fun sectionBreaker(message: StringBuilder) {
    message.appendLine(SECTION_BREAKER).appendLine("")
  }
}
