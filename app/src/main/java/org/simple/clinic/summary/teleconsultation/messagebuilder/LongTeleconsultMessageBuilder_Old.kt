package org.simple.clinic.summary.teleconsultation.messagebuilder

import android.content.res.Resources
import com.f2prateek.rx.preferences2.Preference
import org.simple.clinic.R
import org.simple.clinic.bloodsugar.BloodSugarUnitPreference
import org.simple.clinic.medicalhistory.Answer
import org.simple.clinic.summary.PatientTeleconsultationInfo
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.toLocalDateAtZone
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Named

class LongTeleconsultMessageBuilder_Old @Inject constructor(
    private val resources: Resources,
    private val userClock: UserClock,
    @Named("full_date") private val dateFormatter: DateTimeFormatter,
    private val bloodSugarUnitPreference: Preference<BloodSugarUnitPreference>
) : TeleconsultationMessageBuilder {

  companion object {
    private const val LINE_BREAK = "\n"
  }

  /**
   * We are not translating the message because we don't know the preferred language
   * for the recipient.
   */
  override fun message(patientTeleconsultationInfo: PatientTeleconsultationInfo): String {
    val message = StringBuilder("*${patientTeleconsultationInfo.facility.name}* teleconsult request for:")
        .appendLine("")
        .appendLine("")
        .appendLine("*Patient record*:")
        .appendLine("https://app.simple.org/patient/${patientTeleconsultationInfo.patientUuid}")
        .appendLine("")

    addDiagnosisSectionToMessage(patientTeleconsultationInfo, message)
    addBloodPressuresSectionToMessage(patientTeleconsultationInfo, message)
    addBloodSugarsSectionToMessage(patientTeleconsultationInfo, message)
    addPrescriptionsSectionToMessage(patientTeleconsultationInfo, message)

    if (patientTeleconsultationInfo.bpPassport.isNullOrBlank().not()) {
      message.appendLine("*BP Passport*: ${patientTeleconsultationInfo.bpPassport}")
          .appendLine("")
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
      message.appendLine("*Current medicines*:")
          .appendLine(medicines)
          .appendLine("")
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
            val bloodSugarUnit = resources.getString(it.reading.displayUnit(bloodSugarUnitPreference.get()))
            val displayValue = it.reading.displayValue(bloodSugarUnitPreference.get())

            "$bloodSugarType ${displayValue}${it.reading.displayUnitSeparator}${bloodSugarUnit} ($bloodSugarRecordedAtDate)"
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

      message.appendLine(bloodPressuresTitle)
          .appendLine(bloodPressures)
          .appendLine("")
    }
  }

  private fun addDiagnosisSectionToMessage(
      patientTeleconsultationInfo: PatientTeleconsultationInfo,
      message: StringBuilder
  ) {
    val hyperTensionTitle = resources.getString(R.string.patientsummary_contact_doctor_diagnosis_hypertension)
    val diabetesTitle = resources.getString(R.string.patientsummary_contact_doctor_diagnosis_diabetes)

    val diagnosedWithHypertension = patientTeleconsultationInfo.medicalHistory.diagnosedWithHypertension
    val diagnosedWithDiabetes = patientTeleconsultationInfo.medicalHistory.diagnosedWithDiabetes

    if (diagnosedWithHypertension.isAnswered || diagnosedWithDiabetes.isAnswered) {
      message.appendLine(resources.getString(R.string.patientsummary_contact_doctor_diagnosis))
    }

    if (diagnosedWithHypertension.isAnswered) {
      message.appendLine("$hyperTensionTitle ${textForDiagnosisAnswer(diagnosedWithHypertension)}")
    }

    if (diagnosedWithDiabetes.isAnswered) {
      message.appendLine("$diabetesTitle ${textForDiagnosisAnswer(diagnosedWithDiabetes)}")
    }

    message.appendLine("")
  }

  private fun textForDiagnosisAnswer(answer: Answer): String {
    return when (answer) {
      Answer.Yes -> resources.getString(R.string.patientsummary_contact_doctor_diagnosis_answer_yes)
      Answer.No -> resources.getString(R.string.patientsummary_contact_doctor_diagnosis_answer_no)
      else -> ""
    }
  }
}
