package org.simple.clinic.summary.teleconsultation

import android.content.res.Resources
import org.simple.clinic.R
import org.simple.clinic.bloodsugar.BloodSugarMeasurementType
import org.simple.clinic.bloodsugar.Fasting
import org.simple.clinic.bloodsugar.HbA1c
import org.simple.clinic.bloodsugar.PostPrandial
import org.simple.clinic.bloodsugar.Random
import org.simple.clinic.bloodsugar.Unknown
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

    if (patientTeleconsultationInfo.bloodSugars.isNotEmpty()) {
      val bloodSugars = patientTeleconsultationInfo
          .bloodSugars.joinToString(separator = LINE_BREAK) {
            val bloodSugarRecordedAtDate = dateFormatter.format(it.recordedAt.toLocalDateAtZone(userClock.zone))
            val bloodSugarType = textForBloodSugarType(it.reading.type)
            val bloodSugarUnit = unitForBloodSugarType(it.reading.type)

            "$bloodSugarType ${it.reading.displayValue}${it.reading.displayUnitSeparator}${bloodSugarUnit} ($bloodSugarRecordedAtDate)"
          }

      val bloodSugarsSize = patientTeleconsultationInfo.bloodSugars.size
      val bloodSugarsTitle = resources
          .getQuantityString(R.plurals.patientsummary_contact_doctor_patient_info_blood_sugars, bloodSugarsSize, bloodSugarsSize.toString())

      message.appendln(bloodSugarsTitle)
          .appendln(bloodSugars)
          .appendln("")
    }

    if (patientTeleconsultationInfo.prescriptions.isNotEmpty()) {
      val medicines = patientTeleconsultationInfo
          .prescriptions.joinToString(separator = LINE_BREAK) { "${it.name} ${it.dosage}" }
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

  private fun textForBloodSugarType(type: BloodSugarMeasurementType): String {
    return when (type) {
      Random -> resources.getString(R.string.patientsummary_contact_doctor_bloodsugartype_rbs)
      PostPrandial -> resources.getString(R.string.patientsummary_contact_doctor_bloodsugartype_ppbs)
      Fasting -> resources.getString(R.string.patientsummary_contact_doctor_bloodsugartype_fbs)
      HbA1c -> resources.getString(R.string.patientsummary_contact_doctor_bloodsugartype_hba1c)
      is Unknown -> ""
    }
  }

  private fun unitForBloodSugarType(type: BloodSugarMeasurementType): String {
    return when (type) {
      Random, PostPrandial, Fasting -> resources.getString(R.string.patientsummary_contact_doctor_unit_type_mg_dl)
      HbA1c -> resources.getString(R.string.patientsummary_contact_doctor_unit_type_percentage)
      is Unknown -> ""
    }
  }
}
