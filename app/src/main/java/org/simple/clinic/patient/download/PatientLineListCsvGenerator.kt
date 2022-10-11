package org.simple.clinic.patient.download

import android.content.res.Resources
import androidx.annotation.StringRes
import com.opencsv.CSVWriter
import org.simple.clinic.R
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.di.AppScope
import org.simple.clinic.di.DateFormatter
import org.simple.clinic.di.DateFormatter.Type.MonthName
import org.simple.clinic.medicalhistory.Answer.Yes
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.PatientLineListRow
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.PatientStatus
import org.simple.clinic.patient.PatientStatus.Dead
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.toLocalDateAtZone
import java.io.ByteArrayOutputStream
import java.io.OutputStreamWriter
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named

private enum class PatientLineListColumn(@StringRes val value: Int) {
  SNO(R.string.patient_line_list_column_sno),
  NAME(R.string.patient_line_list_column_name),
  SEX(R.string.patient_line_list_column_sex),
  AGE(R.string.patient_line_list_column_age),
  REGISTRATION_DATE(R.string.patient_line_list_column_registration_date),
  REGISTRATION_FACILITY(R.string.patient_line_list_column_registration_facility),
  ASSIGNED_FACILITY(R.string.patient_line_list_column_assigned_facility),
  BP_PASSPORT(R.string.patient_line_list_column_bp_passport),
  STREET_ADDRESS(R.string.patient_line_list_column_street_address),
  VILLAGE(R.string.patient_line_list_column_village),
  PHONE(R.string.patient_line_list_column_phone),
  DIAGNOSIS(R.string.patient_line_list_column_diagnosis),
  CONTROL_STATUS(R.string.patient_line_list_column_control_status),
  STATUS(R.string.patient_line_list_column_patient_status)
}

class PatientLineListCsvGenerator @Inject constructor(
    @AppScope private val resources: Resources,
    private val patientRepository: PatientRepository,
    private val userClock: UserClock,
    @Named("date_for_user_input") private val dateFormatter: DateTimeFormatter,
    @DateFormatter(MonthName) private val monthNameDateFormatter: DateTimeFormatter
) {

  fun generate(
      facilityId: UUID,
      bpCreatedAfter: LocalDate,
      bpCreatedBefore: LocalDate
  ): ByteArrayOutputStream {
    val outputStream = ByteArrayOutputStream()
    val csvWriter = CSVWriter(OutputStreamWriter(outputStream))

    val title = resources.getString(R.string.patient_line_list_title)

    val reportStartMonthName = monthNameDateFormatter.format(bpCreatedAfter)
    val reportEndMonthName = monthNameDateFormatter.format(bpCreatedBefore)

    val columnNames = PatientLineListColumn
        .values()
        .map {
          if (it == PatientLineListColumn.CONTROL_STATUS) {
            resources.getString(it.value, reportStartMonthName, reportEndMonthName)
          } else {
            resources.getString(it.value)
          }
        }
        .toTypedArray()

    csvWriter.writeNext(arrayOf(title), false)
    csvWriter.writeNext(columnNames, false)

    var offset = 0
    val patientLineListCount = patientRepository.patientLineListCount(
        facilityId = facilityId,
        bpCreatedAfter = bpCreatedAfter,
        bpCreatedBefore = bpCreatedBefore
    )

    while (offset < patientLineListCount) {
      val patientLineList = patientRepository.patientLineListRows(
          facilityId = facilityId,
          bpCreatedAfter = bpCreatedAfter,
          bpCreatedBefore = bpCreatedBefore,
          offset = offset
      )

      patientLineList.forEachIndexed { index, patientLineListRow ->
        val patientLineListRowColumns = patientLineListRowColumns(index, offset, patientLineListRow)

        csvWriter.writeNext(patientLineListRowColumns, false)
      }

      offset += patientLineList.size
    }

    csvWriter.close()

    return outputStream
  }

  private fun patientLineListRowColumns(
      index: Int,
      offset: Int,
      patientLineListRow: PatientLineListRow
  ): Array<String> {
    val columns = mutableListOf<String>()

    // Since Kotlin list index start from 0, we are adding 1 to (index + current offset)
    // to offset for S.No in the entire patient line list
    val sNo = (index + offset) + 1
    columns.add(sNo.toString())

    with(patientLineListRow) {
      columns.add(patientName)
      columns.add(patientGender())

      val age = age.estimateAge(userClock).toString()
      columns.add(age)

      val registeredDate = dateFormatter.format(registrationDate.toLocalDateAtZone(userClock.zone))
      columns.add(registeredDate)

      columns.add(registrationFacilityName.orEmpty())
      if (registrationFacilityName != assignedFacilityName) {
        columns.add(assignedFacilityName.orEmpty())
      } else {
        columns.add("")
      }

      columns.add(bpPassport())
      columns.add(streetAddress.orEmpty())
      columns.add(colonyOrVillage.orEmpty())
      columns.add(patientPhoneNumber.orEmpty())
      columns.add(diagnosisStatus())

      val controlStatus = bpControlStatus(latestBloodPressureMeasurement, status)
      columns.add(controlStatus)

      columns.add(patientStatus())
    }

    return columns.toTypedArray()
  }

  private fun PatientLineListRow.bpPassport(): String {
    return bpPassport?.identifier?.displayValue().orEmpty()
  }

  private fun PatientLineListRow.patientStatus(): String {
    val stringRes = if (status == Dead) {
      R.string.patient_line_list_status_died
    } else {
      R.string.patient_line_list_status_none
    }

    return resources.getString(stringRes)
  }

  private fun PatientLineListRow.patientGender(): String {
    val stringRes = when (gender) {
      Gender.Female -> R.string.patient_line_list_gender_female
      Gender.Male -> R.string.patient_line_list_gender_male
      Gender.Transgender -> R.string.patient_line_list_gender_trans
      is Gender.Unknown -> R.string.patient_line_list_gender_unknown
    }

    return resources.getString(stringRes)
  }

  private fun PatientLineListRow.diagnosisStatus(): String {
    val stringRes = when {
      diagnosedWithHypertension == Yes && diagnosedWithDiabetes == Yes -> {
        R.string.patient_line_list_diagnosis_status_both
      }

      diagnosedWithHypertension == Yes -> R.string.patient_line_list_diagnosis_status_htn
      diagnosedWithDiabetes == Yes -> R.string.patient_line_list_diagnosis_status_dm
      else -> R.string.patient_line_list_diagnosis_status_no_diagnosis
    }

    return resources.getString(stringRes)
  }

  private fun bpControlStatus(
      bp: BloodPressureMeasurement?,
      patientStatus: PatientStatus
  ): String {
    if (patientStatus == Dead) return resources.getString(R.string.patient_line_list_no_control_status)

    if (bp == null) return resources.getString(R.string.patient_line_list_missed_visit)

    val systolic = bp.reading.systolic
    val diastolic = bp.reading.diastolic

    val stringRes = if (systolic < 140 && diastolic < 90) {
      R.string.patient_line_list_control_status_controlled
    } else {
      R.string.patient_line_list_control_status_uncontrolled
    }

    return resources.getString(stringRes)
  }
}
