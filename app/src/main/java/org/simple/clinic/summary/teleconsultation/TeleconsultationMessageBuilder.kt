package org.simple.clinic.summary.teleconsultation

import android.content.res.Resources
import org.simple.clinic.R
import org.simple.clinic.medicalhistory.Answer
import org.simple.clinic.summary.PatientTeleconsultationInfo
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.toLocalDateAtZone
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Named

class TeleconsultationMessageBuilder @Inject constructor(
    private val resources: Resources,
    private val userClock: UserClock,
    @Named("full_date") private val dateFormatter: DateTimeFormatter
) {

  companion object {
    private const val LINE_BREAK = "\n"
  }

  fun message(patientTeleconsultationInfo: PatientTeleconsultationInfo): String {
    val message = StringBuilder("*${patientTeleconsultationInfo.facility.name}* teleconsult request for:")
        .appendln("")
        .appendln("")
        .appendln("*Patient record*:")
        .appendln("https://app.simple.org/patient/${patientTeleconsultationInfo.patientUuid}")
        .appendln("")

    addDiagnosisSectionToMessage(patientTeleconsultationInfo, message)
    addBloodPressuresSectionToMessage(patientTeleconsultationInfo, message)
    addBloodSugarsSectionToMessage(patientTeleconsultationInfo, message)
    addPrescriptionsSectionToMessage(patientTeleconsultationInfo, message)

    if (patientTeleconsultationInfo.bpPassport.isNullOrBlank().not()) {
      message.appendln("*BP Passport*: ${patientTeleconsultationInfo.bpPassport}")
          .appendln("")
    }

    return message.toString()
  }

  private fun addPrescriptionsSectionToMessage(
      patientTeleconsultationInfo: PatientTeleconsultationInfo,
      message: StringBuilder
  ) {
    if (patientTeleconsultationInfo.prescriptions.isNotEmpty()) {
      val medicines = patientTeleconsultationInfo
          .prescriptions.joinToString(separator = LINE_BREAK) { "${it.name} ${it.dosage}" }
      message.appendln("*Current medicines*:")
          .appendln(medicines)
          .appendln("")
    }
  }

  private fun addBloodSugarsSectionToMessage(
      patientTeleconsultationInfo: PatientTeleconsultationInfo,
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

      message.appendln(bloodSugarsTitle)
          .appendln(bloodSugars)
          .appendln("")
    }
  }

  private fun addBloodPressuresSectionToMessage(
      patientTeleconsultationInfo: PatientTeleconsultationInfo,
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

      message.appendln(bloodPressuresTitle)
          .appendln(bloodPressures)
          .appendln("")
    }
  }

  private fun addDiagnosisSectionToMessage(patientTeleconsultationInfo: PatientTeleconsultationInfo, message: StringBuilder) {
    val hyperTensionTitle = resources.getString(R.string.patientsummary_contact_doctor_diagnosis_hypertension)
    val diabetesTitle = resources.getString(R.string.patientsummary_contact_doctor_diagnosis_diabetes)

    val diagnosedWithHypertension = patientTeleconsultationInfo.medicalHistory.diagnosedWithHypertension
    val diagnosedWithDiabetes = patientTeleconsultationInfo.medicalHistory.diagnosedWithDiabetes

    if (diagnosedWithHypertension.isAnswered || diagnosedWithDiabetes.isAnswered) {
      message.appendln(resources.getString(R.string.patientsummary_contact_doctor_diagnosis))
    }

    if (diagnosedWithHypertension.isAnswered) {
      message.appendln("$hyperTensionTitle ${textForDiagnosisAnswer(diagnosedWithHypertension)}")
    }

    if (diagnosedWithDiabetes.isAnswered) {
      message.appendln("$diabetesTitle ${textForDiagnosisAnswer(diagnosedWithDiabetes)}")
    }

    message.appendln("")
  }

  private fun textForDiagnosisAnswer(answer: Answer): String {
    return when (answer) {
      Answer.Yes -> resources.getString(R.string.patientsummary_contact_doctor_diagnosis_answer_yes)
      Answer.No -> resources.getString(R.string.patientsummary_contact_doctor_diagnosis_answer_no)
      else -> ""
    }
  }
}
